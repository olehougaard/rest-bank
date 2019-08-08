package dk.via.bank.dao;

import dk.via.bank.model.Account;
import dk.via.bank.model.AccountNumber;
import dk.via.bank.model.Customer;
import dk.via.bank.model.Money;
import dk.via.bank.model.parameters.AccountSpecification;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Path("/customers/{cpr}/accounts")
public class AccountDAOService  {
	private DatabaseHelper<Account> helper;

	public AccountDAOService() {
		this(DatabaseHelper.JDBC_URL, DatabaseHelper.USERNAME, DatabaseHelper.PASSWORD);
	}
	
	public AccountDAOService(String jdbcURL, String username, String password) {
		helper = new DatabaseHelper<>(jdbcURL, username, password);
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Account createAccount(AccountSpecification specification) {
		int regNumber = specification.getRegNumber();
		String cpr = specification.getCustomerCpr();
		String currency = specification.getCurrency();
		final List<Integer> keys = helper.executeUpdateWithGeneratedKeys("INSERT INTO Account(reg_number, customer, currency) VALUES (?, ?, ?)", 
				regNumber, cpr, currency);
		return readAccount(regNumber + "" + keys.get(0));
	}
	
	public static class AccountMapper implements DataMapper<Account>{
		@Override
		public Account create(ResultSet rs) throws SQLException {
			AccountNumber accountNumber = new AccountNumber(rs.getInt("reg_number"), rs.getLong("account_number"));
			BigDecimal balance = rs.getBigDecimal("balance");
			String currency = rs.getString("currency");
			return new Account(accountNumber, new Money(balance, currency));
		}
		
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
    public Collection<Account> readAccountsFor(@PathParam("cpr") String cpr) {
		return helper.map(new AccountMapper(), "SELECT * FROM Account WHERE customer = ? AND active", cpr) ;
	}

	@GET
	@Path("{accountNumber}")
	@Produces(MediaType.APPLICATION_JSON)
    public Account readAccount(@PathParam("accountNumber") String accountString) {
		return getAccount(AccountNumber.fromString(accountString));
	}

	private Account getAccount(AccountNumber accountNumber) {
		return helper.mapSingle(new AccountMapper(), "SELECT * FROM Account WHERE reg_number = ? AND account_number = ? AND active",
				accountNumber.getRegNumber(), accountNumber.getAccountNumber());
	}

	@PUT
	@Path("{accountNumber}")
    public Response updateAccount(@PathParam("accountNumber") String accountString, Account account) {
		AccountNumber accountNumber = AccountNumber.fromString(accountString);
		if (getAccount(accountNumber) == null) {
			return Response.status(403).build();
		} else {
			helper.executeUpdate("UPDATE ACCOUNT SET balance = ?, currency = ? WHERE reg_number = ? AND account_number = ? AND active",
					account.getBalance().getAmount(), account.getSettledCurrency(), accountNumber.getRegNumber(), accountNumber.getAccountNumber());
			return Response.status(200).build();
		}
	}

    public void deleteAccount(Account account) {
		helper.executeUpdate("UPDATE ACCOUNT SET active = FALSE WHERE reg_number = ? AND account_number = ?", 
				account.getAccountNumber().getRegNumber(), account.getAccountNumber().getAccountNumber());
	}
}

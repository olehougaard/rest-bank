package dk.via.bank.dao;

import dk.via.bank.model.Account;
import dk.via.bank.model.AccountNumber;
import dk.via.bank.model.Customer;
import dk.via.bank.model.Money;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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

	public Account createAccount(int regNumber, Customer customer, String currency) {
		final List<Integer> keys = helper.executeUpdateWithGeneratedKeys("INSERT INTO Account(reg_number, customer, currency) VALUES (?, ?, ?)", 
				regNumber, customer.getCpr(), currency);
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
		AccountNumber accountNumber = new AccountNumber(Integer.parseInt(accountString.substring(0, 4)), Integer.parseInt(accountString.substring(4)));
		return helper.mapSingle(new AccountMapper(), "SELECT * FROM Account WHERE reg_number = ? AND account_number = ? AND active",
				accountNumber.getRegNumber(), accountNumber.getAccountNumber());
	}

    public void updateAccount(Account account) {
		helper.executeUpdate("UPDATE ACCOUNT SET balance = ?, currency = ? WHERE reg_number = ? AND account_number = ? AND active", 
				account.getBalance().getAmount(), account.getSettledCurrency(), account.getAccountNumber().getRegNumber(), account.getAccountNumber().getAccountNumber());
	}

    public void deleteAccount(Account account) {
		helper.executeUpdate("UPDATE ACCOUNT SET active = FALSE WHERE reg_number = ? AND account_number = ?", 
				account.getAccountNumber().getRegNumber(), account.getAccountNumber().getAccountNumber());
	}
}

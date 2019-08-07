package dk.via.bank.dao;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.inject.Inject;

import dk.via.bank.model.*;
import dk.via.bank.model.transaction.*;

public class TransactionDAOService extends UnicastRemoteObject implements TransactionDAO {
	private static final String DEPOSIT = "Deposit";
	private static final String TRANSFER = "Transfer";
	private static final String WITHDRAWAL = "Withdrawal";
	
	private static final long serialVersionUID = 1L;
	private DataHelper<Transaction> helper;
	private AccountDAO accounts;
	
	@Inject
	public TransactionDAOService(AccountDAO accounts, DataProvider provider) throws RemoteException {
		this.accounts = accounts;
		this.helper = provider.createHelper(Transaction.class);
	}
	
	private class TransactionMapper implements DataMapper<Transaction> {
		@Override
		public Transaction create(ResultSet rs) throws SQLException, RemoteException {
			Money amount = new Money(rs.getBigDecimal("amount"), rs.getString("currency"));
			String text = rs.getString("transaction_text");
			Account primary = readAccount(rs, "primary_reg_number", "primary_account_number");
			switch(rs.getString("transaction_type")) {
			case DEPOSIT:
				return new DepositTransaction(amount, primary, text);
			case WITHDRAWAL:
				return new WithdrawTransaction(amount, primary, text);
			case TRANSFER:
				Account secondaryAccount = readAccount(rs, "secondary_reg_number", "secondary_account_number");
				return new TransferTransaction(amount, primary, secondaryAccount, text);
			default:
				return null;
			}
		}

		private Account readAccount(ResultSet rs, String regNumberAttr, String acctNumberAttr) throws SQLException, RemoteException {
			return accounts.read(new AccountNumber(rs.getInt(regNumberAttr), rs.getLong(acctNumberAttr)));
		}
	}
	
	private class TransactionCreator implements TransactionVisitor {
		@Override
		public void visit(DepositTransaction transaction) throws RemoteException {
			Money amount = transaction.getAmount();
			AccountNumber primaryAccount = transaction.getAccount().getAccountNumber();
			helper.executeUpdate("INSERT INTO Transaction(amount, currency, transaction_type, transaction_text, primary_reg_number, primary_account_number) VALUES (?, ?, ?, ?, ?, ?)", 
					amount.getAmount(), amount.getCurrency(), DEPOSIT, transaction.getText(), primaryAccount.getRegNumber(), primaryAccount.getAccountNumber());
		}

		@Override
		public void visit(WithdrawTransaction transaction) throws RemoteException {
			Money amount = transaction.getAmount();
			AccountNumber primaryAccount = transaction.getAccount().getAccountNumber();
			helper.executeUpdate("INSERT INTO Transaction(amount, currency, transaction_type, transaction_text, primary_reg_number, primary_account_number) VALUES (?, ?, ?, ?, ?, ?)", 
					amount.getAmount(), amount.getCurrency(), WITHDRAWAL, transaction.getText(), primaryAccount.getRegNumber(), primaryAccount.getAccountNumber());
		}

		@Override
		public void visit(TransferTransaction transaction) throws RemoteException {
			Money amount = transaction.getAmount();
			AccountNumber primaryAccount = transaction.getAccount().getAccountNumber();
			AccountNumber secondaryAccount = transaction.getRecipient().getAccountNumber();
			helper.executeUpdate("INSERT INTO Transaction(amount, currency, transaction_type, transaction_text, primary_reg_number, primary_account_number, secondary_reg_number, secondary_account_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 
					amount.getAmount(), amount.getCurrency(), TRANSFER, transaction.getText(), primaryAccount.getRegNumber(), primaryAccount.getAccountNumber(), secondaryAccount.getRegNumber(), secondaryAccount.getAccountNumber());
		}
	}
	
	private final TransactionCreator creator = new TransactionCreator();
	
	@Override
	public void create(Transaction transaction) throws RemoteException {
		transaction.accept(creator);
	}

	@Override
	public Transaction read(int transactionId) throws RemoteException {
		return helper.mapSingle(new TransactionMapper(), "SELECT * FROM Transaction WHERE transaction_id = ?", transactionId);
	}

	@Override
	public List<Transaction> readAllFor(Account account) throws RemoteException {
		AccountNumber accountNumber = account.getAccountNumber();
		return helper.map(new TransactionMapper(), 
				"SELECT * FROM Transaction WHERE (primary_reg_number = ? AND primary_account_number = ?) OR (secondary_reg_number = ? AND secondary_account_number = ?)",
				accountNumber.getRegNumber(), accountNumber.getAccountNumber(),accountNumber.getRegNumber(), accountNumber.getAccountNumber());
	}

	@Override
	public void deleteFor(Account account) throws RemoteException {
		AccountNumber accountNumber = account.getAccountNumber();
		helper.executeUpdate("DELETE FROM Transaction WHERE (primary_reg_number = ? AND primary_account_number = ?) OR (secondary_reg_number = ? AND secondary_account_number = ?)",
				accountNumber.getRegNumber(), accountNumber.getAccountNumber(),accountNumber.getRegNumber(), accountNumber.getAccountNumber());
		
	}
}

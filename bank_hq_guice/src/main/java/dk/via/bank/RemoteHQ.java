package dk.via.bank;

import com.google.inject.Inject;
import dk.via.bank.dao.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteHQ extends UnicastRemoteObject implements HeadQuarters {
	private static final long serialVersionUID = 1L;
	
	private ExchangeRateDAO exchangeDAO;
	private AccountDAO accountDAO;
	private CustomerDAO customerDAO;
	private TransactionDAO transactionDAO;

	@Inject
	public RemoteHQ(ExchangeRateDAO exchangeDAO, AccountDAO accountDAO, TransactionDAO transactionDAO, CustomerDAO customerDAO) throws RemoteException {
		this.exchangeDAO = exchangeDAO;
		this.accountDAO = accountDAO;
		this.transactionDAO = transactionDAO;
		this.customerDAO = customerDAO;
	}

	@Override
	public ExchangeRateDAO getExchangeDAO() throws RemoteException {
		return exchangeDAO;
	}

	@Override
	public AccountDAO getAccountDAO() throws RemoteException {
		return accountDAO;
	}

	@Override
	public CustomerDAO getCustomerDAO() throws RemoteException {
		return customerDAO;
	}

	@Override
	public TransactionDAO getTransactionDAO() throws RemoteException {
		return transactionDAO;
	}
}

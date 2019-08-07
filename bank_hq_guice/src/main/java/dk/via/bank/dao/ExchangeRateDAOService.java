package dk.via.bank.dao;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.google.inject.Inject;

import dk.via.bank.model.ExchangeRate;

public class ExchangeRateDAOService extends UnicastRemoteObject implements ExchangeRateDAO {
	private static final long serialVersionUID = 1L;
	private DataHelper<BigDecimal> helper;

	@Inject
	public ExchangeRateDAOService(DataProvider provider) throws RemoteException {
		helper = provider.createHelper(BigDecimal.class);
	}

	@Override
	public ExchangeRate getExchangeRate(String fromCurrency, String toCurrency) throws RemoteException {
		BigDecimal rate = helper.mapSingle((rs)->rs.getBigDecimal(1), "SELECT rate FROM Exchange_rates WHERE from_currency = ? AND to_currency = ?", fromCurrency, toCurrency);
		return new ExchangeRate(fromCurrency, toCurrency, rate);
	}
}

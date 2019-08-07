package dk.via.bank.dao;

import java.rmi.RemoteException;

import com.google.inject.Inject;

import dk.via.bank.JdbcUrl;
import dk.via.bank.Password;
import dk.via.bank.Username;

public class HelperProvider implements DataProvider {
	private String jdbcURL;
	private String username;
	private String password;
	
	@Inject
	public HelperProvider(@JdbcUrl String jdbcURL, @Username String username, @Password String password) {
		this.jdbcURL = jdbcURL;
		this.username = username;
		this.password = password;
	}

	@Override
	public<T> DataHelper<T> createHelper(Class<T> cl) throws RemoteException {
		return new DatabaseHelper<T>(jdbcURL, username, password);
	}
}

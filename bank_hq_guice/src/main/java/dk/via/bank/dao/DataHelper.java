package dk.via.bank.dao;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DataHelper<T> {

	ResultSet executeQuery(Connection connection, String sql, Object... parameters) throws SQLException;

	int executeUpdate(String sql, Object... parameters) throws RemoteException;

	List<Integer> executeUpdateWithGeneratedKeys(String sql, Object... parameters) throws RemoteException;

	T mapSingle(DataMapper<T> mapper, String sql, Object... parameters) throws RemoteException;

	List<T> map(DataMapper<T> mapper, String sql, Object... parameters) throws RemoteException;

}
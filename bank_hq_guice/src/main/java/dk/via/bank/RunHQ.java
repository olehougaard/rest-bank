package dk.via.bank;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dk.via.bank.dao.HeadQuarters;

public class RunHQ {
	public static void main(String[] args) throws AccessException, RemoteException {
		Injector injector = Guice.createInjector(new BankModule());
		HeadQuarters hq = injector.getInstance(HeadQuarters.class);
		Registry registry = LocateRegistry.createRegistry(1099);
		registry.rebind("HQ", hq);
	}
}

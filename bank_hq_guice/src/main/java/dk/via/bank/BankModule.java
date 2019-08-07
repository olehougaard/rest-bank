package dk.via.bank;

import com.google.inject.AbstractModule;

public class BankModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(String.class).annotatedWith(JdbcUrl.class).toInstance("jdbc:postgresql://localhost:5432/postgres?currentSchema=bank");
		bind(String.class).annotatedWith(Username.class).toInstance("postgres");
		bind(String.class).annotatedWith(Password.class).toInstance("password");
		
		bind(DataProvider.class).to(HelperProvider.class);
		bind(ExchangeRateDAO.class).to(ExchangeRateDAOService.class);
		bind(AccountDAO.class).to(AccountDAOService.class);
		bind(TransactionDAO.class).to(TransactionDAOService.class);
		bind(CustomerDAO.class).to(CustomerDAOService.class);
		bind(HeadQuarters.class).to(RemoteHQ.class);
	}
}

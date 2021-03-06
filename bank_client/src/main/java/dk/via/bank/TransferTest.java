package dk.via.bank;

import dk.via.bank.model.Account;
import dk.via.bank.model.Customer;
import dk.via.bank.model.Money;
import dk.via.bank.model.transaction.AbstractTransaction;
import dk.via.bank.model.transaction.DepositTransaction;
import dk.via.bank.model.transaction.TransferTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransferTest {
	private Branch branch;
	private Account primaryAccount;
	private Account secondaryAccount;
	private Customer customer;
	private Customer other;

	@Before
	public void setUp() throws Exception {
		URL wsdl = new URL("http://localhost:8090/branch?wsdl");
		QName name = new QName("http://bank.via.dk/", "RemoteBranchService");
		Service service = Service.create(wsdl, name);
		branch = service.getPort(Branch.class);
		customer = branch.getCustomer("1234567890");
		primaryAccount = branch.createAccount(customer, "DKK");
		assertNotNull(primaryAccount);
		other = branch.getCustomer("1122334455");
		secondaryAccount = branch.createAccount(other, "EUR");
	}
	
	@After
	public void tearDown() {
		if (primaryAccount != null) branch.cancelAccount(primaryAccount);
		if (secondaryAccount != null) branch.cancelAccount(secondaryAccount);
	}
	
	@Test
	public void test() {
		Money startingAmount = new Money(new BigDecimal(10000), "DKK");
		Money transferAmount = new Money(new BigDecimal(1000), "DKK");
		Money remainingAmount = new Money(new BigDecimal(9000), "DKK");
		List<AbstractTransaction> primaryTransactionsBefore = branch.getTransactionsFor(primaryAccount);
		List<AbstractTransaction> secondaryTransactionsBefore = branch.getTransactionsFor(secondaryAccount);
		branch.execute(new DepositTransaction(startingAmount, primaryAccount));
		primaryAccount = branch.getAccount(customer, primaryAccount.getAccountNumber());
		assertEquals(startingAmount, primaryAccount.getBalance());
		branch.execute(new TransferTransaction(transferAmount, primaryAccount, secondaryAccount));
		primaryAccount = branch.getAccount(customer, primaryAccount.getAccountNumber());
		secondaryAccount = branch.getAccount(other, secondaryAccount.getAccountNumber());
		assertEquals(remainingAmount, primaryAccount.getBalance());
		assertEquals(branch.exchange(transferAmount, secondaryAccount.getSettledCurrency()), secondaryAccount.getBalance());
		assertEquals(primaryTransactionsBefore.size() + 2, branch.getTransactionsFor(primaryAccount).size());
		assertEquals(secondaryTransactionsBefore.size() + 1, branch.getTransactionsFor(secondaryAccount).size());
	}
}

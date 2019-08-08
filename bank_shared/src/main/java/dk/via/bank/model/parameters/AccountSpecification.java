package dk.via.bank.model.parameters;

public class AccountSpecification {
    private int regNumber;
    private String customerCpr;
    private String currency;

    public AccountSpecification() {
    }

    public AccountSpecification(int regNumber, String customerCpr, String currency) {
        this.regNumber = regNumber;
        this.customerCpr = customerCpr;
        this.currency = currency;
    }

    public int getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(int regNumber) {
        this.regNumber = regNumber;
    }

    public String getCustomerCpr() {
        return customerCpr;
    }

    public void setCustomerCpr(String customerCpr) {
        this.customerCpr = customerCpr;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

package banking;

/** Shared state and behaviour for all bank account types. */
public abstract class BankAccount {
    private final String accountNumber;
    private final String holderName;
    private double balance;

    protected BankAccount(String accountNumber, String holderName, double openingBalance) {
        if (accountNumber == null || accountNumber.isBlank()) throw new IllegalArgumentException("Account number is required.");
        if (holderName == null || holderName.isBlank()) throw new IllegalArgumentException("Account holder name is required.");
        if (!Double.isFinite(openingBalance) || openingBalance < 0) throw new IllegalArgumentException("Opening balance cannot be negative.");
        this.accountNumber = accountNumber;
        this.holderName = holderName.trim();
        this.balance = openingBalance;
    }

    public final String getAccountNumber() { return accountNumber; }
    public final String getHolderName() { return holderName; }
    public final double getBalance() { return balance; }

    /** Controlled state update for deposits, withdrawals, transfers and loans. */
    protected final void changeBalance(double amount) {
        double updated = balance + amount;
        if (!Double.isFinite(updated)) throw new IllegalArgumentException("Balance is outside the supported range.");
        balance = updated;
    }

    public abstract void deposit(double amount);
    public abstract void withdraw(double amount);
    public abstract double calculateInterest();
    public abstract double getAvailableFunds();
    public abstract String getAccountType();

    @Override
    public String toString() {
        return String.format("%-10s | %-9s | %-22s | Balance: M%,.2f", accountNumber, getAccountType(), holderName, balance);
    }
}

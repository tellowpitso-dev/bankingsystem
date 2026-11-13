package banking;

/** Savings accounts retain at least M100.00 after any withdrawal. */
public final class SavingsAccount extends BankAccount implements Loanable {
    public static final double MINIMUM_BALANCE = 100.00;
    private static final double ANNUAL_INTEREST_RATE = 0.05;
    private static final double LOAN_LIMIT = 50_000.00;

    public SavingsAccount(String accountNumber, String holderName, double openingBalance) {
        super(accountNumber, holderName, openingBalance);
    }

    @Override public void deposit(double amount) {
        requirePositive(amount, "Deposit");
        changeBalance(amount);
    }

    @Override public void withdraw(double amount) {
        requirePositive(amount, "Withdrawal");
        if (getBalance() - amount < MINIMUM_BALANCE)
            throw new IllegalStateException("Savings accounts must retain at least M100.00.");
        changeBalance(-amount);
    }

    @Override public double calculateInterest() {
        double interest = getBalance() * ANNUAL_INTEREST_RATE;
        changeBalance(interest);
        return interest;
    }

    @Override public double getAvailableFunds() { return Math.max(0, getBalance() - MINIMUM_BALANCE); }
    @Override public String getAccountType() { return "Savings"; }
    @Override public double getLoanLimit() { return LOAN_LIMIT; }

    @Override public void requestLoan(double amount) {
        requirePositive(amount, "Loan request");
        if (amount > getLoanLimit()) throw new IllegalArgumentException("Savings loan limit is M50,000.00.");
        changeBalance(amount);
    }

    private static void requirePositive(double amount, String operation) {
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException(operation + " amount must be greater than zero.");
    }
}

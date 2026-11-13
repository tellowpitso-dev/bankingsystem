package banking;

/** Current accounts may draw down to M2,000.00 below zero. */
public final class CurrentAccount extends BankAccount implements Loanable {
    public static final double OVERDRAFT_LIMIT = 2_000.00;
    private static final double LOAN_LIMIT = 100_000.00;

    public CurrentAccount(String accountNumber, String holderName, double openingBalance) {
        super(accountNumber, holderName, openingBalance);
    }

    @Override public void deposit(double amount) {
        requirePositive(amount, "Deposit");
        changeBalance(amount);
    }

    @Override public void withdraw(double amount) {
        requirePositive(amount, "Withdrawal");
        if (getBalance() - amount < -OVERDRAFT_LIMIT)
            throw new IllegalStateException("Withdrawal exceeds the M2,000.00 overdraft limit.");
        changeBalance(-amount);
    }

    @Override public double calculateInterest() { return 0.00; }
    @Override public double getAvailableFunds() { return Math.max(0, getBalance() + OVERDRAFT_LIMIT); }
    @Override public String getAccountType() { return "Current"; }
    @Override public double getLoanLimit() { return LOAN_LIMIT; }

    @Override public void requestLoan(double amount) {
        requirePositive(amount, "Loan request");
        if (amount > getLoanLimit()) throw new IllegalArgumentException("Current account loan limit is M100,000.00.");
        changeBalance(amount);
    }

    private static void requirePositive(double amount, String operation) {
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException(operation + " amount must be greater than zero.");
    }
}

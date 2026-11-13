package banking;

/** Implemented by account types that support loan requests. */
public interface Loanable {
    double getLoanLimit();
    void requestLoan(double amount);
}

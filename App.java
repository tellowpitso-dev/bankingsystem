import banking.BankAccount;
import banking.CurrentAccount;
import banking.Loanable;
import banking.SavingsAccount;
import banking.TransactionLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Console entry point. BankAccount references demonstrate polymorphism. */
public class App {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final List<BankAccount> ACCOUNTS = new ArrayList<>();
    private static int nextAccountNumber = 1001;

    public static void main(String[] args) {
        System.out.println("Welcome to the Console Banking System");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = INPUT.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> displayAllAccounts();
                    case "2" -> createAccount();
                    case "3" -> deposit();
                    case "4" -> withdraw();
                    case "5" -> transfer();
                    case "6" -> calculateInterest();
                    case "7" -> requestLoan();
                    case "8" -> displayAccountDetails();
                    case "9" -> running = false;
                    default -> {
                        System.out.println("Invalid menu choice. Select 1-9.");
                        TransactionLogger.log("N/A", "INVALID_MENU_CHOICE", "N/A", "N/A", "Rejected: " + choice);
                    }
                }
            } catch (RuntimeException e) {
                System.out.println("Operation could not be completed: " + e.getMessage());
            }
        }
        INPUT.close();
        System.out.println("Thank you. The system has closed safely.");
    }

    private static void printMenu() {
        System.out.println("\n========== BANKING MENU ==========");
        System.out.println("1. Display all accounts");
        System.out.println("2. Create account");
        System.out.println("3. Deposit money");
        System.out.println("4. Withdraw money");
        System.out.println("5. Transfer money");
        System.out.println("6. Calculate interest");
        System.out.println("7. Request bank loan");
        System.out.println("8. Display account details");
        System.out.println("9. Exit");
        System.out.print("Choose an option: ");
    }

    private static void createAccount() {
        String type = prompt("Account type (S = Savings, C = Current): ").toUpperCase();
        String holder = prompt("Account holder full name: ");
        double openingBalance = readAmount("Opening balance (M; cannot be negative): ", true);
        String number = String.valueOf(nextAccountNumber++);
        try {
            // Inheritance and polymorphism: subclass objects are stored as BankAccount references.
            BankAccount account;
            if (type.equals("S")) account = new SavingsAccount(number, holder, openingBalance);
            else if (type.equals("C")) account = new CurrentAccount(number, holder, openingBalance);
            else throw new IllegalArgumentException("Account type must be S or C.");
            ACCOUNTS.add(account);
            System.out.println("Created " + account.getAccountType() + " account. Account number: " + number);
            TransactionLogger.log(number, "ACCOUNT_CREATION", money(openingBalance), money(account.getBalance()), "SUCCESS");
        } catch (RuntimeException e) {
            TransactionLogger.log(number, "ACCOUNT_CREATION", money(openingBalance), "N/A", "REJECTED: " + e.getMessage());
            System.out.println("Account not created: " + e.getMessage());
        }
    }

    private static void displayAllAccounts() {
        if (ACCOUNTS.isEmpty()) { System.out.println("No accounts exist yet. Create an account first."); return; }
        System.out.println("\nACCOUNT NO. | TYPE      | ACCOUNT HOLDER         | BALANCE");
        for (BankAccount account : ACCOUNTS) System.out.println(account);
    }

    private static void deposit() {
        BankAccount account = selectAccount("Deposit into");
        double amount = readAmount("Deposit amount (M): ", false);
        try {
            account.deposit(amount); // Overridden account-specific behaviour via BankAccount reference.
            completed(account, "DEPOSIT", amount, "SUCCESS");
        } catch (RuntimeException e) { rejected(account, "DEPOSIT", amount, e); }
    }

    private static void withdraw() {
        BankAccount account = selectAccount("Withdraw from");
        double amount = readAmount("Withdrawal amount (M): ", false);
        try {
            account.withdraw(amount);
            completed(account, "WITHDRAWAL", amount, "SUCCESS");
        } catch (RuntimeException e) { rejected(account, "WITHDRAWAL", amount, e); }
    }

    private static void transfer() {
        BankAccount source = selectAccount("Transfer from");
        BankAccount destination = selectAccount("Transfer to");
        double amount = readAmount("Transfer amount (M): ", false);
        if (source == destination) {
            IllegalArgumentException e = new IllegalArgumentException("Source and destination must be different accounts.");
            rejected(source, "TRANSFER to " + destination.getAccountNumber(), amount, e);
            return;
        }
        try {
            if (amount <= 0) throw new IllegalArgumentException("Transfer amount must be greater than zero.");
            if (amount > source.getAvailableFunds()) throw new IllegalStateException("Insufficient available funds for transfer.");
            // Use overridden withdraw/deposit; roll back source if destination deposit fails.
            source.withdraw(amount);
            try { destination.deposit(amount); }
            catch (RuntimeException e) { source.deposit(amount); throw e; }
            completed(source, "TRANSFER_OUT to " + destination.getAccountNumber(), amount, "SUCCESS");
            completed(destination, "TRANSFER_IN from " + source.getAccountNumber(), amount, "SUCCESS");
            System.out.printf("Transferred M%,.2f from %s to %s.%n", amount, source.getAccountNumber(), destination.getAccountNumber());
        } catch (RuntimeException e) {
            rejected(source, "TRANSFER to " + destination.getAccountNumber(), amount, e);
            rejected(destination, "TRANSFER_IN from " + source.getAccountNumber(), amount, e);
        }
    }

    private static void calculateInterest() {
        BankAccount account = selectAccount("Calculate interest for");
        try {
            double interest = account.calculateInterest();
            completed(account, "INTEREST", interest, "SUCCESS; interest added: " + money(interest));
        } catch (RuntimeException e) { rejected(account, "INTEREST", 0, e); }
    }

    private static void requestLoan() {
        BankAccount account = selectAccount("Request loan for");
        double amount = readAmount("Loan amount (M): ", false);
        try {
            if (!(account instanceof Loanable loanable)) throw new IllegalStateException("This account does not support loans.");
            loanable.requestLoan(amount);
            completed(account, "LOAN", amount, "SUCCESS; limit: " + money(loanable.getLoanLimit()));
        } catch (RuntimeException e) { rejected(account, "LOAN", amount, e); }
    }

    private static void displayAccountDetails() {
        BankAccount account = selectAccount("Display details for");
        System.out.println("\nAccount number: " + account.getAccountNumber());
        System.out.println("Account holder: " + account.getHolderName());
        System.out.println("Account type: " + account.getAccountType());
        System.out.printf("Balance: M%,.2f%n", account.getBalance());
        System.out.printf("Available funds: M%,.2f%n", account.getAvailableFunds());
        if (account instanceof Loanable loanable) System.out.printf("Loan limit: M%,.2f%n", loanable.getLoanLimit());
    }

    private static BankAccount selectAccount(String action) {
        if (ACCOUNTS.isEmpty()) throw new IllegalStateException("There are no accounts. Create one first.");
        displayAllAccounts();
        String number = prompt(action + " — enter account number: ");
        for (BankAccount account : ACCOUNTS) if (account.getAccountNumber().equals(number)) return account;
        TransactionLogger.log(number, "ACCOUNT_SELECTION", "N/A", "N/A", "REJECTED: account not found");
        throw new IllegalArgumentException("No account found with number " + number + ".");
    }

    private static String prompt(String message) { System.out.print(message); return INPUT.nextLine().trim(); }

    private static double readAmount(String message, boolean allowZero) {
        String raw = prompt(message);
        try {
            double value = Double.parseDouble(raw);
            if (!Double.isFinite(value) || (allowZero ? value < 0 : value <= 0))
                throw new NumberFormatException("Amount is outside allowed range.");
            return value;
        } catch (NumberFormatException e) {
            TransactionLogger.log("N/A", "INVALID_NUMERIC_INPUT", raw, "N/A", "REJECTED: enter a valid " + (allowZero ? "non-negative" : "positive") + " number");
            throw new IllegalArgumentException("Enter a valid " + (allowZero ? "non-negative" : "positive") + " amount.");
        }
    }

    private static void completed(BankAccount account, String type, double amount, String message) {
        System.out.printf("%s completed. New balance: M%,.2f%n", type, account.getBalance());
        TransactionLogger.log(account.getAccountNumber(), type, money(amount), money(account.getBalance()), message);
    }

    private static void rejected(BankAccount account, String type, double amount, RuntimeException e) {
        System.out.println("Rejected: " + e.getMessage());
        TransactionLogger.log(account.getAccountNumber(), type, money(amount), money(account.getBalance()), "REJECTED: " + e.getMessage());
    }

    private static String money(double value) { return String.format("M%,.2f", value); }
}

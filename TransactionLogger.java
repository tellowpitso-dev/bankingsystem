package banking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Final utility class: transaction records are appended in the working directory. */
public final class TransactionLogger {
    private static final Path FILE = Path.of("transactions.txt");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private TransactionLogger() { }

    public static void log(String accountNumber, String type, String amount, String balance, String result) {
        String record = String.format("%s | Account: %s | Type: %s | Amount: %s | Balance: %s | Result: %s%n",
                LocalDateTime.now().format(FORMAT), safe(accountNumber), safe(type), safe(amount), safe(balance), safe(result));
        try {
            Files.writeString(FILE, record, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Warning: could not write transaction log: " + e.getMessage());
        }
    }

    private static String safe(String value) { return value == null ? "N/A" : value.replace('|', '/').replace('\n', ' '); }
}

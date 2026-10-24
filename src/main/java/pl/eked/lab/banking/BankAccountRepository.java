package pl.eked.lab.banking;

import java.util.HashMap;
import java.util.Map;

public class BankAccountRepository {
    private final Map<String, BankAccount> bankAccounts = new HashMap<>();

    public synchronized boolean createAccount(String accountNumber, int initialBalance) {
        if (initialBalance < 0)
            return false;

        if (accountNumber == null || accountNumber.isEmpty())
            return false;

        var exists = this.exists(accountNumber);
        if (exists) {
            return false;
        }

        var normalizedAccountNumber = AccountNumberNormalizer.normalize(accountNumber);
        this.bankAccounts.put(normalizedAccountNumber,
            new BankAccount(normalizedAccountNumber, initialBalance));

        return true;
    }

    public synchronized boolean exists(String accountNumber) {
        var normalizedAccountNumber = AccountNumberNormalizer.normalize(accountNumber);
        return this.bankAccounts.containsKey(normalizedAccountNumber);
    }

    public synchronized BankAccount findByAccountNumber(String accountNumber) {
        var normalizedAccountNumber = AccountNumberNormalizer.normalize(accountNumber);
        return this.bankAccounts.get(normalizedAccountNumber);
    }
}

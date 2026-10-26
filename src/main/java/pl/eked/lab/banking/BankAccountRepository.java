package pl.eked.lab.banking;

import java.util.HashMap;
import java.util.Map;

public class BankAccountRepository {
    private final Map<AccountNumber, BankAccount> bankAccounts = new HashMap<>();

    public synchronized boolean createAccount(AccountNumber accountNumber, int initialBalance) {
        var validatedBankAccount = new BankAccount(accountNumber, initialBalance);

        var exists = this.exists(accountNumber);
        if (exists) {
            return false;
        }

        this.bankAccounts.put(validatedBankAccount.getAccountNumber(), validatedBankAccount);

        return true;
    }

    public synchronized boolean exists(AccountNumber accountNumber) {
        return this.bankAccounts.containsKey(accountNumber);
    }

    public synchronized BankAccount findByAccountNumber(AccountNumber accountNumber) {
        return this.bankAccounts.get(accountNumber);
    }
}

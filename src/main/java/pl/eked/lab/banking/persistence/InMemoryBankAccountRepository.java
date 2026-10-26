package pl.eked.lab.banking.persistence;

import java.util.HashMap;
import java.util.Map;
import pl.eked.lab.banking.application.port.output.BankAccountRepository;
import pl.eked.lab.banking.domain.BankAccount;
import pl.eked.lab.banking.domain.AccountNumber;

public class InMemoryBankAccountRepository implements BankAccountRepository {
    private final Map<AccountNumber, BankAccount> bankAccounts = new HashMap<>();

    @Override
    public synchronized boolean createAccount(AccountNumber accountNumber, int initialBalance) {
        var validatedBankAccount = new BankAccount(accountNumber, initialBalance);

        var exists = this.exists(accountNumber);
        if (exists) {
            return false;
        }

        this.bankAccounts.put(validatedBankAccount.getAccountNumber(), validatedBankAccount);

        return true;
    }

    @Override
    public synchronized boolean exists(AccountNumber accountNumber) {
        return this.bankAccounts.containsKey(accountNumber);
    }

    @Override
    public synchronized BankAccount findByAccountNumber(AccountNumber accountNumber) {
        return this.bankAccounts.get(accountNumber);
    }
}

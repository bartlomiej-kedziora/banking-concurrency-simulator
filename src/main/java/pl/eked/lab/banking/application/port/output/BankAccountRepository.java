package pl.eked.lab.banking.application.port.output;

import pl.eked.lab.banking.domain.AccountNumber;
import pl.eked.lab.banking.domain.BankAccount;

public interface BankAccountRepository {
    boolean createAccount(AccountNumber accountNumber, int initialBalance);

    boolean exists(AccountNumber accountNumber);

    BankAccount findByAccountNumber(AccountNumber accountNumber);
}

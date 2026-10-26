package pl.eked.lab.banking.application.transfer;

import pl.eked.lab.banking.domain.BankAccount;

public interface TransferExecutor {
    boolean execute(BankAccount fromAccount, BankAccount toAccount, int amount);
}

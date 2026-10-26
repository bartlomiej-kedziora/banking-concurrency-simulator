package pl.eked.lab.banking.application.transfer;

import pl.eked.lab.banking.application.port.output.BankAccountRepository;
import pl.eked.lab.banking.domain.AccountNumber;

public class BankTransferService {
    private final BankAccountRepository repository;
    private final TransferExecutor transferExecutor;

    public BankTransferService(
        BankAccountRepository repository,
        TransferExecutor transferExecutor
    ) {
        this.repository = repository;
        this.transferExecutor = transferExecutor;
    }

    boolean transferFunds(AccountNumber fromAccountNumber, AccountNumber toAccountNumber, int amount) {
        if (fromAccountNumber == null || toAccountNumber == null)
            return false;

        if (fromAccountNumber.equals(toAccountNumber))
            return false;

        var fromAccount = repository.findByAccountNumber(fromAccountNumber);
        var toAccount = repository.findByAccountNumber(toAccountNumber);

        if (fromAccount == null || toAccount == null)
            return false;

        return transferExecutor.execute(fromAccount, toAccount, amount);
    }
}

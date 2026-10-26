package pl.eked.lab.banking.concurrency;

import pl.eked.lab.banking.application.TransferExecutor;
import pl.eked.lab.banking.domain.BankAccount;

public class SynchronizedTransferExecutor implements TransferExecutor {

    @Override
    public boolean execute(BankAccount fromAccount, BankAccount toAccount, int amount) {
        var lockOrder = LockOrder.of(fromAccount, toAccount); // prevent deadlock

        synchronized (lockOrder.first()) {
            synchronized (lockOrder.second()) {
                if (!fromAccount.canWithdraw(amount) || !toAccount.canDeposit(amount)) {
                    return false;
                }

                if (!fromAccount.withdraw(amount)) {
                    throw new IllegalStateException("Withdrawal failed after successful validation");
                }

                if (!toAccount.deposit(amount)) {
                    throw new IllegalStateException("Deposit failed after successful validation");
                }

                return true;
            }
        }
    }

    private record LockOrder(
        BankAccount first,
        BankAccount second
    ) {
        static LockOrder of(BankAccount accountA, BankAccount accountB) {
            var lockOrder =  accountA.getAccountNumber().compareTo(accountB.getAccountNumber());
            if (lockOrder < 0)
                return new LockOrder(accountA, accountB);

            return new LockOrder(accountB, accountA);
        }
    }
}

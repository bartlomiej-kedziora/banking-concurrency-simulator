package pl.eked.lab.banking.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.eked.lab.banking.application.transfer.BankTransferService;
import pl.eked.lab.banking.application.transfer.SynchronizedTransferExecutor;
import pl.eked.lab.banking.domain.AccountNumber;
import pl.eked.lab.banking.persistence.InMemoryBankAccountRepository;

class ConcurrentBankTransfersIntegrationTest {
    private InMemoryBankAccountRepository repository;
    private SynchronizedTransferExecutor transferExecutor;
    private BankTransferService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBankAccountRepository();
        transferExecutor = new SynchronizedTransferExecutor();
        service = new BankTransferService(repository, transferExecutor);
    }

    /**
     * Transfers expectations:
     *  _____________________________________________________________________________________________________
     * |account |init balance   |final balance  |actions                                |calculations       |
     * -----------------------------------------------------------------------------------------------------|
     * |A       |1000           |800            |transfer to B 700                      |1000-700+200+300   |
     * |B       |1000           |1100           |transfer to C 400, transfer to A 200   |1000+700-400-200   |
     * |C       |1000           |1100           |transfer to A 300                      |1000+400-300       |
     * -----------------------------------------------------------------------------------------------------
     */
    @Test
    void shouldPreserveTotalBalanceWhenMultipleAccountsTransferConcurrently()
        throws InterruptedException {
        //given
        var initialBalance = 1000;
        var accountNumberA = Fixtures.VALID_ACCOUNT_NUMBER_A;
        var accountNumberB = Fixtures.VALID_ACCOUNT_NUMBER_B;
        var accountNumberC = Fixtures.VALID_ACCOUNT_NUMBER_C;

        repository.createAccount(accountNumberA, initialBalance);
        repository.createAccount(accountNumberB, initialBalance);
        repository.createAccount(accountNumberC, initialBalance);

        var accountA = repository.findByAccountNumber(accountNumberA);
        var accountB = repository.findByAccountNumber(accountNumberB);
        var accountC = repository.findByAccountNumber(accountNumberC);

        int balancesSumBeforeTransactions = accountA.getBalance() + accountB.getBalance() + accountC.getBalance();

        var transferSuccess = new Boolean[4];
        var transferFailures = new Throwable[4];

        var startLatch = new CountDownLatch(1);

        var thread1 = new Thread(() -> {
            try {
                startLatch.await();
                transferSuccess[0] = service.transferFunds(accountNumberA, accountNumberB, 700);
            } catch (InterruptedException e) {
                transferFailures[0] = e;
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                transferFailures[0] = e;
            }
        });

        var thread2 = new Thread(() -> {
            try {
                startLatch.await();
                transferSuccess[1] = service.transferFunds(accountNumberB, accountNumberC, 400);
            } catch (InterruptedException e) {
                transferFailures[1] = e;
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                transferFailures[1] = e;
            }
        });

        var thread3 = new Thread(() -> {
            try {
                startLatch.await();
                transferSuccess[2] = service.transferFunds(accountNumberC, accountNumberA, 300);
            } catch (InterruptedException e) {
                transferFailures[2] = e;
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                transferFailures[2] = e;
            }
        });

        var thread4 = new Thread(() -> {
            try {
                startLatch.await();
                transferSuccess[3] = service.transferFunds(accountNumberB, accountNumberA, 200);
            } catch (InterruptedException e) {
                transferFailures[3] = e;
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                transferFailures[3] = e;
            }
        });

        //when
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        startLatch.countDown();
        thread1.join(5000);
        thread2.join(5000);
        thread3.join(5000);
        thread4.join(5000);

        //then
        assertFalse(thread1.isAlive(), "Thread 1 did not finish");
        assertFalse(thread2.isAlive(), "Thread 2 did not finish");
        assertFalse(thread3.isAlive(), "Thread 3 did not finish");
        assertFalse(thread4.isAlive(), "Thread 4 did not finish");

        assertNull(transferFailures[0], () -> "Transfer 1 failed: " + transferFailures[0]);
        assertNull(transferFailures[1], () -> "Transfer 2 failed: " + transferFailures[1]);
        assertNull(transferFailures[2], () -> "Transfer 3 failed: " + transferFailures[2]);
        assertNull(transferFailures[3], () -> "Transfer 4 failed: " + transferFailures[3]);

        assertEquals(Boolean.TRUE, transferSuccess[0]);
        assertEquals(Boolean.TRUE, transferSuccess[1]);
        assertEquals(Boolean.TRUE, transferSuccess[2]);
        assertEquals(Boolean.TRUE, transferSuccess[3]);

        int balancesSumAfterTransactions = accountA.getBalance() + accountB.getBalance() + accountC.getBalance();
        assertEquals(800, accountA.getBalance());
        assertEquals(1100, accountB.getBalance());
        assertEquals(1100, accountC.getBalance());
        assertEquals(balancesSumBeforeTransactions, balancesSumAfterTransactions);
    }

    @Test
    void shouldAllowOnlyOneTransferWhenConcurrentWithdrawalsExceedAvailableBalance()
        throws InterruptedException {
        //given
        var accountNumberA = Fixtures.VALID_ACCOUNT_NUMBER_A;
        var accountNumberB = Fixtures.VALID_ACCOUNT_NUMBER_B;
        var accountNumberC = Fixtures.VALID_ACCOUNT_NUMBER_C;

        repository.createAccount(accountNumberA, 1000);
        repository.createAccount(accountNumberB, 0);
        repository.createAccount(accountNumberC, 0);

        var accountA = repository.findByAccountNumber(accountNumberA);
        var accountB = repository.findByAccountNumber(accountNumberB);
        var accountC = repository.findByAccountNumber(accountNumberC);

        int balancesSumBeforeTransactions = accountA.getBalance() + accountB.getBalance() + accountC.getBalance();

        var transferResults = new Boolean[2];
        var transferFailures = new Throwable[2];

        var startLatch = new CountDownLatch(1);

        var thread1 = new Thread(() -> {
            try {
                startLatch.await();
                transferResults[0] = service.transferFunds(accountNumberA, accountNumberB, 700);
            } catch (InterruptedException e) {
                transferFailures[0] = e;
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                transferFailures[0] = e;
            }
        });

        var thread2 = new Thread(() -> {
            try {
                startLatch.await();
                transferResults[1] = service.transferFunds(accountNumberA, accountNumberC, 600);
            } catch (InterruptedException e) {
                transferFailures[1] = e;
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                transferFailures[1] = e;
            }
        });

        //when
        thread1.start();
        thread2.start();

        startLatch.countDown();
        thread1.join(5000);
        thread2.join(5000);

        //then
        assertFalse(thread1.isAlive(), "Thread 1 did not finish");
        assertFalse(thread2.isAlive(), "Thread 2 did not finish");

        assertNull(transferFailures[0], () -> "Transfer 1 failed: " + transferFailures[0]);
        assertNull(transferFailures[1], () -> "Transfer 2 failed: " + transferFailures[1]);

        var isTransfer1Success = Boolean.TRUE.equals(transferResults[0]);
        var isTransfer2Success = Boolean.TRUE.equals(transferResults[1]);
        var isOneTransferSuccess = isTransfer1Success || isTransfer2Success;
        var areBothTransfersSuccess = isTransfer1Success && isTransfer2Success;

        assertTrue(isOneTransferSuccess);
        assertFalse(areBothTransfersSuccess, "Both transactions are success");

        var expectedAccountABalance = isTransfer1Success ? 1000 - 700 : 1000 - 600;
        var expectedAccountBBalance = isTransfer1Success ? 700 : 0;
        var expectedAccountCBalance = isTransfer1Success ? 0 : 600;
        int balancesSumAfterTransactions = accountA.getBalance() + accountB.getBalance() + accountC.getBalance();
        assertEquals(expectedAccountABalance, accountA.getBalance());
        assertEquals(expectedAccountBBalance, accountB.getBalance());
        assertEquals(expectedAccountCBalance, accountC.getBalance());
        assertEquals(balancesSumBeforeTransactions, balancesSumAfterTransactions);
    }

    private static final class Fixtures {
        private static final AccountNumber VALID_ACCOUNT_NUMBER_A =
            new AccountNumber("00 1111 2222 3333 4444 5555 6666");
        private static final AccountNumber VALID_ACCOUNT_NUMBER_B =
            new AccountNumber("01 1111 2222 3333 4444 5555 6666");
        private static final AccountNumber VALID_ACCOUNT_NUMBER_C =
            new AccountNumber("02 1111 2222 3333 4444 5555 6666");
    }
}

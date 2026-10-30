package pl.eked.lab.banking.application.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.eked.lab.banking.domain.AccountNumber;
import pl.eked.lab.banking.domain.BankAccount;

class SynchronizedTransferExecutorTest {
    private final SynchronizedTransferExecutor transferExecutor = new SynchronizedTransferExecutor();

    @Nested
    class SuccessfulTransfers {
        @Test
        void shouldTransferFundsWhenBothAccountsAllowTransfer() {
            //given
            var bankAccountA = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);
            var bankAccountB = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_B, 0);

            //when
            var result = transferExecutor.execute(bankAccountA, bankAccountB, 700);

            //then
            assertTrue(result);
            assertEquals(300, bankAccountA.getBalance());
            assertEquals(700, bankAccountB.getBalance());
        }
    }

    @Nested
    class RejectedTransfers {
        @Test
        void shouldRejectTransferWhenSourceAccountHasInsufficientFunds() {
            //given
            var bankAccountA = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);
            var bankAccountB = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_B, 0);

            //when
            var result = transferExecutor.execute(bankAccountA, bankAccountB, 2000);

            //then
            assertFalse(result);
            assertEquals(1000, bankAccountA.getBalance());
            assertEquals(0, bankAccountB.getBalance());

        }

        @Test
        void shouldRejectTransferWhenDestinationBalanceWouldOverflow() {
            //given
            var bankAccountA = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);
            var bankAccountB = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_B, Integer.MAX_VALUE - 100);

            //when
            var result = transferExecutor.execute(bankAccountA, bankAccountB, 200);

            //then
            assertFalse(result);
            assertEquals(1000, bankAccountA.getBalance());
            assertEquals(Integer.MAX_VALUE - 100, bankAccountB.getBalance());
        }

        @Test
        void shouldRejectTransferWhenAmountIsZero() {
            //given
            var bankAccountA = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);
            var bankAccountB = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_B, 0);

            //when
            var result = transferExecutor.execute(bankAccountA, bankAccountB, 0);

            //then
            assertFalse(result);
            assertEquals(1000, bankAccountA.getBalance());
            assertEquals(0, bankAccountB.getBalance());
        }

        @Test
        void shouldRejectTransferWhenAmountIsNegative() {
            //given
            var bankAccountA = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);
            var bankAccountB = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_B, 0);

            //when
            var result = transferExecutor.execute(bankAccountA, bankAccountB, -1);

            //then
            assertFalse(result);
            assertEquals(1000, bankAccountA.getBalance());
            assertEquals(0, bankAccountB.getBalance());
        }
    }

    @Nested
    class InvalidArguments {
        @Test
        void shouldThrowNullPointerExceptionWhenSourceAccountIsNull() {
            //given
            var bankAccountB = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_B, 0);

            //when & then
            assertThrows(
                NullPointerException.class,
                () -> transferExecutor.execute(null, bankAccountB, 700)
            );
            assertEquals(0, bankAccountB.getBalance());

        }

        @Test
        void shouldThrowNullPointerExceptionWhenDestinationAccountIsNull() {
            //given
            var bankAccountA = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);

            //when & then
            assertThrows(
                NullPointerException.class,
                () -> transferExecutor.execute(bankAccountA, null, 700)
            );
            assertEquals(1000, bankAccountA.getBalance());
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenAccountInstancesAreTheSameWithTheSameAccountNumber() {
            //given
            var bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);

            //when & then
            assertThrows(
                IllegalArgumentException.class,
                () -> transferExecutor.execute(bankAccount, bankAccount, 700)
            );
            assertEquals(1000, bankAccount.getBalance());
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenAccountInstancesAreDifferentWithTheSameAccountNumber() {
            //given
            var bankAccountA = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);
            var bankAccountB = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);

            //when & then
            assertThrows(
                IllegalArgumentException.class,
                () -> transferExecutor.execute(bankAccountA, bankAccountB, 700)
            );
            assertEquals(1000, bankAccountA.getBalance());
        }
    }

    @Nested
    class ConcurrentTransfers {
        @Test
        void shouldAllowOnlyOneTransferWhenConcurrentTransfersExceedAvailableBalance()
            throws InterruptedException {
            //given
            var bankAccountA = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1001);
            var bankAccountB = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_B, 0);
            var totalBalanceBeforeTransfer = 1001;

            var results = new boolean[2];

            var threadA =  new Thread(() -> {
                results[0] = transferExecutor.execute(bankAccountA, bankAccountB, 700);
            });
            var threadB =  new Thread(() -> {
                results[1] = transferExecutor.execute(bankAccountA, bankAccountB, 600);
            });

            //when
            threadA.start();
            threadB.start();
            threadA.join(5000);
            threadB.join(5000);

            //then
            assertFalse(threadA.isAlive(), "Thread A did not finish");
            assertFalse(threadB.isAlive(), "Thread B did not finish");

            var isOneTransferSuccess = results[0] || results[1];
            var areBothTransfersSuccess = results[0] && results[1];
            var expectedBalanceA = results[0] ? 1001 - 700 : 1001 - 600;
            var expectedBalanceB = results[1] ? 600 : 700;
            var totalBalanceAfterTransfer = bankAccountA.getBalance() + bankAccountB.getBalance();
            assertTrue(isOneTransferSuccess, "Neither transfer succeeded");
            assertFalse(areBothTransfersSuccess, "Both transfers success");
            assertEquals(expectedBalanceA, bankAccountA.getBalance());
            assertEquals(expectedBalanceB, bankAccountB.getBalance());
            assertEquals(totalBalanceBeforeTransfer, totalBalanceAfterTransfer);
        }

        @Test
        void shouldCompleteOppositeDirectionTransfersWithoutDeadlock() throws InterruptedException {
            //given
            var bankAccountA = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_A, 1000);
            var bankAccountB = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER_B, 1000);
            var totalBalanceBeforeTransfer = 2000;

            var results = new boolean[2];

            var threadA =  new Thread(() -> {
                results[0] = transferExecutor.execute(bankAccountA, bankAccountB, 700);
            });
            var threadB =  new Thread(() -> {
                results[1] = transferExecutor.execute(bankAccountB, bankAccountA, 600);
            });

            var expectedBalanceA = 900;
            var expectedBalanceB = 1100;

            //when
            threadA.start();
            threadB.start();
            threadA.join(5000);
            threadB.join(5000);

            //then
            assertFalse(threadA.isAlive(), "Thread A did not finish");
            assertFalse(threadB.isAlive(), "Thread B did not finish");

            var totalBalanceAfterTransfer = bankAccountA.getBalance() + bankAccountB.getBalance();
            assertTrue(results[0], "Transfer from A to B failed");
            assertTrue(results[1], "Transfer from B to A failed");
            assertEquals(expectedBalanceA, bankAccountA.getBalance());
            assertEquals(expectedBalanceB, bankAccountB.getBalance());
            assertEquals(totalBalanceBeforeTransfer, totalBalanceAfterTransfer);
        }
    }

    private static final class Fixtures {
        private static final AccountNumber VALID_ACCOUNT_NUMBER_A =
            new AccountNumber("00 1111 2222 3333 4444 5555 6666");
        private static final AccountNumber VALID_ACCOUNT_NUMBER_B =
            new AccountNumber("01 1111 2222 3333 4444 5555 6666");
    }
}

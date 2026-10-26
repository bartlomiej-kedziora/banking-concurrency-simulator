package pl.eked.lab.banking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BankAccountTest {

    @Nested
    class Creation {
        @Test
        void shouldRejectNullAccountNumber() {
            // given
            AccountNumber givenAccountNumber = null;
            var givenBalance = 1;
            var expectedErrorMsg = "Account number cannot be null";

            //when & then
            var exception = assertThrows(
                NullPointerException.class,
                () -> new BankAccount(givenAccountNumber, givenBalance)
            );

            assertEquals(expectedErrorMsg, exception.getMessage());
        }

        @Test
        void shouldRejectNegativeInitialBalance() {
            // given
            var givenAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER;
            var givenBalance = -1;
            var expectedErrorMsg = "Invalid balance";

            //when & then
            var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BankAccount(new AccountNumber(givenAccountNumber), givenBalance)
            );

            assertEquals(expectedErrorMsg, exception.getMessage());
        }

        @Test
        void shouldCreateAccountWithZeroBalance() {
            // given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = 0;

            //when
            var result = new BankAccount(givenAccountNumber, givenBalance);

            //then
            assertEquals(givenAccountNumber, result.getAccountNumber());
            assertEquals(givenBalance, result.getBalance());
        }
    }

    @Nested
    class Withdrawal {
        @Test
        void withdrawShouldReduceBalanceWhenFundsAreSufficient() {
            //given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = 1000;
            var bankAccount = new BankAccount(givenAccountNumber, givenBalance);

            //when
            var isWithdrawn = bankAccount.withdraw(700);
            var result = bankAccount.getBalance();

            //then
            assertEquals(300, result);
            assertTrue(isWithdrawn);
        }

        @Test
        void withdrawShouldNotChangeBalanceWhenFundsAreInsufficient() {
            //given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = 1000;
            var bankAccount = new BankAccount(givenAccountNumber, givenBalance);

            //when
            var isWithdrawn = bankAccount.withdraw(1200);
            var result = bankAccount.getBalance();

            //then
            assertEquals(givenBalance, result);
            assertFalse(isWithdrawn);
        }

        @Test
        void withdrawShouldRejectZeroAmount() {
            //given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = 1000;
            var bankAccount = new BankAccount(givenAccountNumber, givenBalance);

            //when
            var isWithdrawn = bankAccount.withdraw(0);
            var result = bankAccount.getBalance();

            //then
            assertEquals(givenBalance, result);
            assertFalse(isWithdrawn);
        }
    }

    @Nested
    class Deposit {
        @Test
        void depositShouldIncreaseBalanceWhenAmountIsPositive() {
            //given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = 1000;
            var bankAccount = new BankAccount(givenAccountNumber, givenBalance);

            //when
            var isDepositSuccessful = bankAccount.deposit(500);
            var result = bankAccount.getBalance();

            //then
            assertEquals(1500, result);
            assertTrue(isDepositSuccessful);
        }

        @Test
        void depositShouldRejectZeroAmount() {
            //given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = 1000;
            var bankAccount = new BankAccount(givenAccountNumber, givenBalance);
            var givenDeposit = 0;

            //when
            var isDepositSuccessful = bankAccount.deposit(givenDeposit);
            var result = bankAccount.getBalance();

            //then
            assertEquals(givenBalance, result);
            assertFalse(isDepositSuccessful);
        }

        @Test
        void depositShouldRejectWhenBalanceOverflows() {
            //given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = Integer.MAX_VALUE;
            var bankAccount = new BankAccount(givenAccountNumber, givenBalance);
            var givenDeposit = 1;

            //when
            var isDepositSuccessful = bankAccount.deposit(givenDeposit);
            var result = bankAccount.getBalance();

            //when & then
            assertEquals(givenBalance, result);
            assertFalse(isDepositSuccessful);
        }
    }

    @Nested
    class Concurrency {
        @Test
        void concurrentWithdrawalsShouldNotOverdrawAccount() throws InterruptedException {
            //given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = 1000;
            var bankAccount = new BankAccount(givenAccountNumber, givenBalance);
            var givenWithdrawA = 700;
            var givenWithdrawB = 500;

            Fixtures.BankAccountWithdrawTask bankAccountWithdrawTaskA =
                new Fixtures.BankAccountWithdrawTask(bankAccount, givenWithdrawA);
            Thread threadA = new Thread(bankAccountWithdrawTaskA);

            Fixtures.BankAccountWithdrawTask bankAccountWithdrawTaskB =
                new Fixtures.BankAccountWithdrawTask(bankAccount, givenWithdrawB);
            Thread threadB = new Thread(bankAccountWithdrawTaskB);

            var matchExpected = Set.of(300, 500);

            //when
            threadA.start();
            threadB.start();
            threadA.join();
            threadB.join();
            var result = bankAccount.getBalance();
            var isExactlyOneWithdrawalSuccessful =
                bankAccountWithdrawTaskA.isWithdrawalSuccessful() ^
                    bankAccountWithdrawTaskB.isWithdrawalSuccessful(); // XOR -> true only if both are different

            //then
            assertTrue(matchExpected.contains(result), "Unexpected balance: " + result);
            assertTrue(isExactlyOneWithdrawalSuccessful);
        }

        @Test
        void concurrentDepositsShouldIncreaseBalanceCorrectly() throws InterruptedException {
            //given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = 1000;
            var bankAccount = new BankAccount(givenAccountNumber, givenBalance);
            var givenDepositA = 500;
            var givenDepositB = 300;

            Fixtures.BankAccountDepositTask bankAccountDepositTaskA =
                new Fixtures.BankAccountDepositTask(bankAccount, givenDepositA);
            Thread threadA = new Thread(bankAccountDepositTaskA);

            Fixtures.BankAccountDepositTask bankAccountDepositTaskB =
                new Fixtures.BankAccountDepositTask(bankAccount, givenDepositB);
            Thread threadB = new Thread(bankAccountDepositTaskB);

            //when
            threadA.start();
            threadB.start();
            threadA.join();
            threadB.join();
            var result = bankAccount.getBalance();
            var isDepositSuccessfulA = bankAccountDepositTaskA.isDepositSuccessful();
            var isDepositSuccessfulB = bankAccountDepositTaskB.isDepositSuccessful();

            //then
            assertEquals(1800, result);
            assertTrue(isDepositSuccessfulA);
            assertTrue(isDepositSuccessfulB);
        }

        @Test
        void concurrentDepositAndWithdrawalShouldPreserveBalance() throws InterruptedException {
            //given
            var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
            var givenBalance = 1000;
            var bankAccount = new BankAccount(givenAccountNumber, givenBalance);
            var givenDepositA = 500;
            var giventWithdrawB = 700;

            Fixtures.BankAccountDepositTask bankAccountDepositTaskA =
                new Fixtures.BankAccountDepositTask(bankAccount, givenDepositA);
            Thread threadA = new Thread(bankAccountDepositTaskA);

            Fixtures.BankAccountWithdrawTask bankAccountWithdrawTaskB =
                new Fixtures.BankAccountWithdrawTask(bankAccount, giventWithdrawB);
            Thread threadB = new Thread(bankAccountWithdrawTaskB);

            //when
            threadA.start();
            threadB.start();
            threadA.join();
            threadB.join();
            var result = bankAccount.getBalance();
            var isDepositSuccessfulA = bankAccountDepositTaskA.isDepositSuccessful();
            var isWithdrawalSuccessfulB = bankAccountWithdrawTaskB.isWithdrawalSuccessful();

            //then
            assertEquals(800, result);
            assertTrue(isDepositSuccessfulA);
            assertTrue(isWithdrawalSuccessfulB);
        }
    }



    private static final class Fixtures {
        private static final String VALID_ACCOUNT_NUMBER = "12 3456 7890 1234 5678 9012 3456";
        private static class BankAccountDepositTask implements Runnable {
            private final BankAccount bankAccount;
            private final int amount;
            private boolean depositSuccessful;

            public BankAccountDepositTask(BankAccount bankAccount, int amount) {
                this.bankAccount = bankAccount;
                this.amount = amount;
            }

            @Override
            public void run() {
                depositSuccessful = bankAccount.deposit(amount);
            }

            public boolean isDepositSuccessful() {
                return depositSuccessful;
            }
        }

        private static class BankAccountWithdrawTask implements Runnable {
            private final BankAccount bankAccount;
            private final int amount;
            private boolean withdrawalSuccessful;

            public BankAccountWithdrawTask(BankAccount bankAccount, int amount) {
                this.bankAccount = bankAccount;
                this.amount = amount;
            }

            @Override
            public void run() {
                withdrawalSuccessful = bankAccount.withdraw(amount);
            }

            public boolean isWithdrawalSuccessful() {
                return withdrawalSuccessful;
            }
        }
    }
}
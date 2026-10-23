package pl.eked.lab.banking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class BankAccountTest {
    private BankAccount bankAccount;

    @Test
    void withdrawShouldReduceBalanceWhenFundsAreSufficient() {
        //given
        var balance = 1000;
        this.bankAccount = new BankAccount(balance);

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
        var balance = 1000;
        this.bankAccount = new BankAccount(balance);

        //when
        var isWithdrawn = bankAccount.withdraw(1200);
        var result = bankAccount.getBalance();

        //then
        assertEquals(balance, result);
        assertFalse(isWithdrawn);
    }

    @Test
    void withdrawShouldRejectZeroAmount() {
        //given
        var balance = 1000;
        this.bankAccount = new BankAccount(balance);

        //when
        var isWithdrawn = bankAccount.withdraw(0);
        var result = bankAccount.getBalance();

        //then
        assertEquals(balance, result);
        assertFalse(isWithdrawn);
    }

    @Test
    void depositShouldIncreaseBalanceWhenAmountIsPositive() {
        //given
        var balance = 1000;
        this.bankAccount = new BankAccount(balance);

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
        var balance = 1000;
        this.bankAccount = new BankAccount(balance);

        //when
        var isDepositSuccessful = bankAccount.deposit(0);
        var result = bankAccount.getBalance();

        //then
        assertEquals(balance, result);
        assertFalse(isDepositSuccessful);
    }

    @Test
    void depositShouldThrowExceptionWhenBalanceOverflows() {
        //given
        var balance = Integer.MAX_VALUE;
        this.bankAccount = new BankAccount(balance);

        //when & then
        assertThrows(ArithmeticException.class, () -> bankAccount.deposit(1));
        var result = bankAccount.getBalance();
        assertEquals(balance, result);
    }

    @Test
    void concurrentWithdrawalsShouldNotOverdrawAccount() throws InterruptedException {
        //given
        var balance = 1000;
        this.bankAccount = new BankAccount(balance);

        Fixtures.BankAccountWithdrawTask bankAccountWithdrawTaskA =
            new Fixtures.BankAccountWithdrawTask(bankAccount, 700);
        Thread threadA = new Thread(bankAccountWithdrawTaskA);

        Fixtures.BankAccountWithdrawTask bankAccountWithdrawTaskB =
            new Fixtures.BankAccountWithdrawTask(bankAccount, 500);
        Thread threadB = new Thread(bankAccountWithdrawTaskB);

        var matchExpected = Set.of(300, 500);

        //when
        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();
        var result = bankAccount.getBalance();
        var isOnlyOneFasterWithdrawalSuccessful =
            bankAccountWithdrawTaskA.isWithdrawalSuccessful() ^
                bankAccountWithdrawTaskB.isWithdrawalSuccessful(); // XOR -> true only if both are different

        //then
        assertTrue(matchExpected.contains(result), "Unexpected balance: " + result);
        assertTrue(isOnlyOneFasterWithdrawalSuccessful);
    }

    @Test
    void concurrentDepositsShouldIncreaseBalanceCorrectly() throws InterruptedException {
        //given
        var balance = 1000;
        this.bankAccount = new BankAccount(balance);

        Fixtures.BankAccountDepositTask bankAccountDepositTaskA =
            new Fixtures.BankAccountDepositTask(bankAccount, 500);
        Thread threadA = new Thread(bankAccountDepositTaskA);

        Fixtures.BankAccountDepositTask bankAccountDepositTaskB =
            new Fixtures.BankAccountDepositTask(bankAccount, 300);
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
        var balance = 1000;
        this.bankAccount = new BankAccount(balance);

        Fixtures.BankAccountDepositTask bankAccountDepositTaskA =
            new Fixtures.BankAccountDepositTask(bankAccount, 500);
        Thread threadA = new Thread(bankAccountDepositTaskA);

        Fixtures.BankAccountWithdrawTask bankAccountWithdrawTaskB =
            new Fixtures.BankAccountWithdrawTask(bankAccount, 700);
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

    private static class Fixtures {
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
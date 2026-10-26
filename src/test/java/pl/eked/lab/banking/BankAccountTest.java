package pl.eked.lab.banking;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class BankAccountTest {
    private BankAccount bankAccount;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
        strings = {
            "1",
            "PL 2222 3333 4444 5555 6666 7777",
            "00 2222 3333 4444 5555 6666 777",
            "00 2222 3333 4444 5555 6666 7777 8",
        }
    )
    void shouldRejectInvalidAccountNumber(String accountNumber) {
        //given & when & then
        assertThrows(IllegalArgumentException.class, () -> new BankAccount(accountNumber, 1));
    }

    @Test
    void shouldRejectNegativeInitialBalance() {
        //given & when & then
        assertThrows(IllegalArgumentException.class, () -> new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, -1));
    }

    @Test
    void shouldCreateAccountWithZeroBalance() {
        //given & when & then
        assertDoesNotThrow(() -> new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, 0));
    }

    @Test
    void withdrawShouldReduceBalanceWhenFundsAreSufficient() {
        //given
        var balance = 1000;
        this.bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, balance);

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
        this.bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, balance);

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
        this.bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, balance);

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
        this.bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, balance);

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
        this.bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, balance);

        //when
        var isDepositSuccessful = bankAccount.deposit(0);
        var result = bankAccount.getBalance();

        //then
        assertEquals(balance, result);
        assertFalse(isDepositSuccessful);
    }

    @Test
    void depositShouldRejectWhenBalanceOverflows() {
        //given
        var balance = Integer.MAX_VALUE;
        this.bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, balance);

        //when
        var isDepositSuccessful = bankAccount.deposit(1);
        var result = bankAccount.getBalance();

        //when & then
        assertEquals(balance, result);
        assertFalse(isDepositSuccessful);
    }

    @Test
    void concurrentWithdrawalsShouldNotOverdrawAccount() throws InterruptedException {
        //given
        var balance = 1000;
        this.bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, balance);

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
        var balance = 1000;
        this.bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, balance);

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
        this.bankAccount = new BankAccount(Fixtures.VALID_ACCOUNT_NUMBER, balance);

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
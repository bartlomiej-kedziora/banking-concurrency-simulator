package pl.eked.lab.banking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BankAccountRepositoryTest {

    private final BankAccountRepository repository = new BankAccountRepository();

    @Test
    void shouldCreateAccount() {
        //given
        var givenAccountNumber = new AccountNumber("00111122223333444455556666");
        var givenInitialBalance = 1000;

        //when
        var result = repository.createAccount(givenAccountNumber, givenInitialBalance);
        var isAccountCreated = repository.exists(givenAccountNumber);
        var foundAccount = repository.findByAccountNumber(givenAccountNumber);

        //then
        assertTrue(result);
        assertTrue(isAccountCreated);
        assertNotNull(foundAccount);
        assertEquals(givenAccountNumber, foundAccount.getAccountNumber() );
        assertEquals(givenInitialBalance, foundAccount.getBalance() );
    }

    @Test
    void shouldRejectDuplicateAccountNumber() {
        //given
        var givenAccountNumber = new AccountNumber("00111122223333444455556666");
        var givenInitialBalance1 = 1000;
        var givenInitialBalance2 = 2000;

        //when
        var result1 = repository.createAccount(givenAccountNumber, givenInitialBalance1);
        var result2 = repository.createAccount(givenAccountNumber, givenInitialBalance2);
        var foundAccount1 = repository.findByAccountNumber(givenAccountNumber);
        //then
        assertTrue(result1);
        assertFalse(result2);
        assertNotNull(foundAccount1);
        assertEquals(givenAccountNumber, foundAccount1.getAccountNumber() );
        assertEquals(givenInitialBalance1, foundAccount1.getBalance() );
    }

    @Test
    void concurrentAccountCreationShouldAllowOnlyOneSuccess() throws InterruptedException {
        //given
        var givenAccountNumber = new AccountNumber("00111122223333444455556666");
        var givenInitialBalance1 = 1000;
        var givenInitialBalance2 = 2000;

        var results = new boolean[2];
        var thread1 = new Thread(() -> {
            results[0] = repository.createAccount(givenAccountNumber, givenInitialBalance1);
        });
        var thread2 = new Thread(() -> {
            results[1] = repository.createAccount(givenAccountNumber, givenInitialBalance2);
        });

        //when
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        var isExactlyOneAccountCreated = results[0] ^ results[1];
        var expectedBalance = results[0] ? givenInitialBalance1 : givenInitialBalance2;
        var foundAccount = repository.findByAccountNumber(givenAccountNumber);

        //then
        assertTrue(isExactlyOneAccountCreated);
        assertNotNull(foundAccount);
        assertEquals(givenAccountNumber, foundAccount.getAccountNumber() );
        assertEquals(expectedBalance, foundAccount.getBalance());
    }

    @Test
    void shouldCreateAccountWithZeroBalance() {
        //given
        var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
        var givenNormalizedAccountNumber = new AccountNumber(Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER);
        var givenInitialBalance = 0;

        //when
        var result = repository.createAccount(givenAccountNumber, givenInitialBalance);
        var foundAccount = repository.findByAccountNumber(givenAccountNumber);

        //then
        assertTrue(result);
        assertNotNull(foundAccount);
        assertEquals(givenNormalizedAccountNumber, foundAccount.getAccountNumber());
        assertEquals(givenInitialBalance, foundAccount.getBalance());
    }

    @Test
    void shouldRejectNegativeInitialBalance() {
        //given
        var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
        var givenInitialBalance = -1;
        var expectedErrorMsg = "Invalid balance";

        //when & then
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.createAccount(givenAccountNumber, givenInitialBalance)
        );

        assertEquals(expectedErrorMsg, exception.getMessage());
    }

    @Test
    void shouldRejectNullAccountNumber() {
        //given
        AccountNumber givenAccountNumber = null;
        var givenInitialBalance = 1000;
        var expectedErrorMsg = "Account number cannot be null";

        //when & then
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> repository.createAccount(givenAccountNumber, givenInitialBalance)
        );

        assertEquals(expectedErrorMsg, exception.getMessage());
    }

    @Test
    void shouldRejectEmptyAccountNumber() {
        //given
        var givenAccountNumber = "";
        var givenInitialBalance = 1000;
        var expectedErrorMsg = "Account number cannot be null or empty";

        //when & then
        var exception = assertThrows(IllegalArgumentException.class,
            () -> repository.createAccount(new AccountNumber(givenAccountNumber), givenInitialBalance)
        );

        assertEquals(expectedErrorMsg, exception.getMessage());
    }

    @Test
    void shouldRejectInvalidAccountNumber() {
        //given
        var givenAccountNumber = "1A";
        var givenInitialBalance = 1000;
        var expectedErrorMsg = "Invalid account number, must contain exactly 26 digits";

        //when & then
        var exception = assertThrows(IllegalArgumentException.class,
            () -> repository.createAccount(new AccountNumber(givenAccountNumber), givenInitialBalance)
        );

        assertEquals(expectedErrorMsg, exception.getMessage());
    }

    @Test
    void shouldFindExistingAccount() {
        //given
        var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
        var givenNormalizedAccountNumber = new AccountNumber(Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER);
        var givenInitialBalance = 100;
        repository.createAccount(givenAccountNumber, givenInitialBalance);

        //when
        var result = repository.findByAccountNumber(givenAccountNumber);

        //then
        assertNotNull(result);
        assertEquals(givenNormalizedAccountNumber, result.getAccountNumber());
        assertEquals(givenInitialBalance, result.getBalance());
    }

    @Test
    void shouldReturnNullForUnknownAccount() {
        //given
        var givenAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);

        //when
        var result = repository.findByAccountNumber(givenAccountNumber);

        //then
        assertNull(result);
    }

    @Test
    void shouldFindAccountRegardlessOfNumberFormatting() {
        //given
        var givenNotNormalizedAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
        var givenNormalizedAccountNumber = new AccountNumber(Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER);
        var givenInitialBalance = 100;
        repository.createAccount(givenNotNormalizedAccountNumber, givenInitialBalance);

        //when
        var result = repository.findByAccountNumber(givenNormalizedAccountNumber);

        //then
        assertNotNull(result);
        assertEquals(givenNormalizedAccountNumber, result.getAccountNumber());
        assertEquals(givenInitialBalance, result.getBalance());
    }

    @Test
    void shouldRejectDuplicateAccountNumberWithDifferentFormatting() {
        //given
        var givenNotNormalizedAccountNumber = new AccountNumber(Fixtures.VALID_ACCOUNT_NUMBER);
        var givenNormalizedAccountNumber = new AccountNumber(Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER);
        var givenInitialBalance1 = 100;
        var givenInitialBalance2 = 200;

        //when
        var isAccountCreatedForNotNormalizedAccountNumber = repository.createAccount(givenNotNormalizedAccountNumber, givenInitialBalance1);
        var isTheSameAccountCreatedForNormalizedAccountNumber = repository.createAccount(givenNormalizedAccountNumber, givenInitialBalance2);
        var foundAccount = repository.findByAccountNumber(givenNotNormalizedAccountNumber);

        //then
        assertTrue(isAccountCreatedForNotNormalizedAccountNumber);
        assertFalse(isTheSameAccountCreatedForNormalizedAccountNumber);
        assertNotNull(foundAccount);
        assertEquals(givenNormalizedAccountNumber, foundAccount.getAccountNumber());
        assertEquals(givenInitialBalance1, foundAccount.getBalance());
    }

    private static final class Fixtures {
        private static final String VALID_ACCOUNT_NUMBER = "12 3456 7890 1234 5678 9012 3456";
        private static final String NORMALIZED_VALID_ACCOUNT_NUMBER = "12345678901234567890123456";
    }
}

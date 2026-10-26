package pl.eked.lab.banking;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        var givenAccountNumber = "00111122223333444455556666";
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
        var givenAccountNumber = "00111122223333444455556666";
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
        var givenAccountNumber = "00111122223333444455556666";
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
        //given & when & then
        assertDoesNotThrow(
            () ->repository.createAccount(Fixtures.VALID_ACCOUNT_NUMBER, 0)
        );
    }

    @Test
    void shouldRejectNegativeInitialBalance() {
        //given & when & then
        assertThrows(IllegalArgumentException.class,
            () ->repository.createAccount(Fixtures.VALID_ACCOUNT_NUMBER, -1)
        );
    }

    @Test
    void shouldRejectNullAccountNumber() {
        //given & when & then
        assertThrows(IllegalArgumentException.class,
            () ->repository.createAccount(null, 1000)
        );
    }

    @Test
    void shouldRejectEmptyAccountNumber() {
        //given & when & then
        assertThrows(IllegalArgumentException.class,
            () ->repository.createAccount("", 1000)
        );
    }

    @Test
    void shouldRejectInvalidAccountNumber() {
        //given & when & then
        assertThrows(IllegalArgumentException.class,
            () ->repository.createAccount("1A", 1000)
        );
    }

    @Test
    void shouldFindExistingAccount() {
        //given
        var givenAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER;
        repository.createAccount(givenAccountNumber, 100);

        //when
        var result = repository.findByAccountNumber(givenAccountNumber);

        //then
        assertNotNull(result);
    }

    @Test
    void shouldReturnNullForUnknownAccount() {
        //given
        var givenAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER;

        //when
        var result = repository.findByAccountNumber(givenAccountNumber);

        //then
        assertNull(result);
    }

    @Test
    void shouldFindAccountRegardlessOfNumberFormatting() {
        //given
        var givenNotNormalizedAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER;
        var givenNormalizedAccountNumber = Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER;
        repository.createAccount(givenNotNormalizedAccountNumber, 100);

        //when
        var result = repository.findByAccountNumber(givenNormalizedAccountNumber);

        //then
        assertNotNull(result);
    }

    @Test
    void shouldRejectDuplicateAccountNumberWithDifferentFormatting() {
        //given
        var givenNotNormalizedAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER;
        var givenNormalizedAccountNumber = Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER;

        //when
        var isAccountCreatedForNotNormalizedAccountNumber = repository.createAccount(givenNotNormalizedAccountNumber, 100);
        var isTheSameAccountCreatedForNormalizedAccountNumber = repository.createAccount(givenNormalizedAccountNumber, 200);

        //then
        assertTrue(isAccountCreatedForNotNormalizedAccountNumber);
        assertFalse(isTheSameAccountCreatedForNormalizedAccountNumber);
    }

    private static final class Fixtures {
        private static final String VALID_ACCOUNT_NUMBER = "12 3456 7890 1234 5678 9012 3456";
        private static final String NORMALIZED_VALID_ACCOUNT_NUMBER = "12345678901234567890123456";
    }
}
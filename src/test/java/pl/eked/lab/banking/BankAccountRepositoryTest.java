package pl.eked.lab.banking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    }

    @Test
    void shouldRejectNegativeInitialBalance() {

    }

    @Test
    void shouldRejectNullAccountNumber() {

    }

    @Test
    void shouldRejectEmptyAccountNumber() {

    }

    @Test
    void shouldRejectInvalidAccountNumber() {

    }

    @Test
    void shouldFindExistingAccount() {

    }

    @Test
    void shouldReturnNullForUnknownAccount() {

    }

    @Test
    void shouldFindAccountRegardlessOfNumberFormatting() {

    }

    @Test
    void shouldRejectDuplicateAccountNumberWithDifferentFormatting() {

    }
}
package pl.eked.lab.banking.application.transfer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eked.lab.banking.application.port.output.BankAccountRepository;
import pl.eked.lab.banking.domain.AccountNumber;
import pl.eked.lab.banking.domain.BankAccount;

@ExtendWith(MockitoExtension.class)
class BankTransferServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransferExecutor transferExecutor;

    private BankTransferService service;

    @BeforeEach
    void setUp() {
        service = new BankTransferService(bankAccountRepository, transferExecutor);
    }

    @Nested
    class SuccessfulTransfers {
        @Test
        void shouldTransferFundsBetweenExistingAccounts() {
            //given
            var fromAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_A;
            var toAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_B;
            var fromAccount = new BankAccount(fromAccountNumber, 1000);
            var toAccount = new BankAccount(toAccountNumber, 1000);
            var transferAmount = 100;

            when(bankAccountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(fromAccount);
            when(bankAccountRepository.findByAccountNumber(toAccountNumber)).thenReturn(toAccount);
            when(transferExecutor.execute(fromAccount, toAccount, transferAmount)).thenReturn(true);

            //when
            var result = service.transferFunds(fromAccountNumber, toAccountNumber, transferAmount);

            //then
            assertTrue(result);
            verify(bankAccountRepository).findByAccountNumber(fromAccountNumber);
            verify(bankAccountRepository).findByAccountNumber(toAccountNumber);
            verify(transferExecutor).execute(fromAccount, toAccount, transferAmount);
        }
    }

    @Nested
    class RejectedTransfers {
        @Test
        void shouldReturnFalseWhenSourceAccountDoesNotExist() {
            //given
            var fromAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_A;
            var toAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_B;
            var toAccount = new BankAccount(toAccountNumber, 1000);
            var transferAmount = 100;

            when(bankAccountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(null);
            when(bankAccountRepository.findByAccountNumber(toAccountNumber)).thenReturn(toAccount);

            //when
            var result = service.transferFunds(fromAccountNumber, toAccountNumber, transferAmount);

            //then
            assertFalse(result);
            verify(bankAccountRepository).findByAccountNumber(fromAccountNumber);
            verify(bankAccountRepository).findByAccountNumber(toAccountNumber);
            verifyNoInteractions(transferExecutor);
        }

        @Test
        void shouldReturnFalseWhenDestinationAccountDoesNotExist() {
            //given
            var fromAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_A;
            var toAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_B;
            var fromAccount = new BankAccount(fromAccountNumber, 1000);
            var transferAmount = 100;

            when(bankAccountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(fromAccount);
            when(bankAccountRepository.findByAccountNumber(toAccountNumber)).thenReturn(null);

            //when
            var result = service.transferFunds(fromAccountNumber, toAccountNumber, transferAmount);

            //then
            assertFalse(result);
            verify(bankAccountRepository).findByAccountNumber(fromAccountNumber);
            verify(bankAccountRepository).findByAccountNumber(toAccountNumber);
            verifyNoInteractions(transferExecutor);
        }

        @Test
        void shouldReturnFalseWhenBothAccountsDoNotExist() {
            //given
            var fromAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_A;
            var toAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_B;
            var transferAmount = 100;

            when(bankAccountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(null);
            when(bankAccountRepository.findByAccountNumber(toAccountNumber)).thenReturn(null);

            //when
            var result = service.transferFunds(fromAccountNumber, toAccountNumber, transferAmount);

            //then
            assertFalse(result);
            verify(bankAccountRepository).findByAccountNumber(fromAccountNumber);
            verify(bankAccountRepository).findByAccountNumber(toAccountNumber);
            verifyNoInteractions(transferExecutor);
        }

        @Test
        void shouldReturnFalseWhenAccountNumbersAreEqual() {
            //given
            var accountNumber = Fixtures.VALID_ACCOUNT_NUMBER_A;
            var transferAmount = 100;

            //when
            var result = service.transferFunds(accountNumber, accountNumber, transferAmount);

            //then
            assertFalse(result);
            verifyNoInteractions(bankAccountRepository);
            verifyNoInteractions(transferExecutor);
        }

        @Test
        void shouldReturnFalseWhenExecutorRejectsTransfer() {
            //given
            var fromAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_A;
            var toAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_B;
            var fromAccount = new BankAccount(fromAccountNumber, 1000);
            var toAccount = new BankAccount(toAccountNumber, 1000);
            var transferAmount = 100;

            when(bankAccountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(fromAccount);
            when(bankAccountRepository.findByAccountNumber(toAccountNumber)).thenReturn(toAccount);
            when(transferExecutor.execute(fromAccount, toAccount, transferAmount)).thenReturn(false);

            //when
            var result = service.transferFunds(fromAccountNumber, toAccountNumber, transferAmount);

            //then
            assertFalse(result);
            verify(bankAccountRepository).findByAccountNumber(fromAccountNumber);
            verify(bankAccountRepository).findByAccountNumber(toAccountNumber);
            verify(transferExecutor).execute(fromAccount, toAccount, transferAmount);
        }
    }

    @Nested
    class InvalidArguments {
        @Test
        void shouldThrowWhenSourceAccountNumberIsNull() {
            //given
            var toAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_B;
            var transferAmount = 100;

            //when & then
            assertThrows(
                NullPointerException.class,
                () -> service.transferFunds(null, toAccountNumber, transferAmount)
            );

            verifyNoInteractions(bankAccountRepository);
            verifyNoInteractions(transferExecutor);
        }

        @Test
        void shouldThrowWhenDestinationAccountNumberIsNull() {
            //given
            var fromAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_A;
            var transferAmount = 100;

            //when & then
            assertThrows(
                NullPointerException.class,
                () -> service.transferFunds(fromAccountNumber, null, transferAmount)
            );

            verifyNoInteractions(bankAccountRepository);
            verifyNoInteractions(transferExecutor);
        }

        @Test
        void shouldThrowWhenBothAccountNumbersAreNull() {
            //given
            var transferAmount = 100;

            //when & then
            assertThrows(
                NullPointerException.class,
                () -> service.transferFunds(null, null, transferAmount)
            );

            verifyNoInteractions(bankAccountRepository);
            verifyNoInteractions(transferExecutor);
        }
    }

    @Nested
    class ExecutionFailures {
        @Test
        void shouldPropagateExceptionWhenExecutorFails() {
            //given
            var fromAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_A;
            var toAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_B;
            var fromAccount = new BankAccount(fromAccountNumber, 1000);
            var toAccount = new BankAccount(toAccountNumber, 1000);
            var transferAmount = 100;
            var givenException = new IllegalStateException("Withdrawal failed after successful validation");

            when(bankAccountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(fromAccount);
            when(bankAccountRepository.findByAccountNumber(toAccountNumber)).thenReturn(toAccount);
            when(transferExecutor.execute(fromAccount, toAccount, transferAmount)).thenThrow(givenException);

            //when & then
            var exception = assertThrows(
                IllegalStateException.class,
                () -> service.transferFunds(fromAccountNumber, toAccountNumber, transferAmount)
            );

            assertSame(givenException, exception);
            verify(bankAccountRepository).findByAccountNumber(fromAccountNumber);
            verify(bankAccountRepository).findByAccountNumber(toAccountNumber);
            verify(transferExecutor).execute(fromAccount, toAccount, transferAmount);
        }

        @Test
        void shouldPropagateExceptionWhenRepositoryFails() {
            //given
            var fromAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_A;
            var toAccountNumber = Fixtures.VALID_ACCOUNT_NUMBER_B;
            var fromAccount = new BankAccount(fromAccountNumber, 1000);
            var transferAmount = 100;
            var givenException = new RuntimeException("Can't access to storage");

            when(bankAccountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(fromAccount);
            when(bankAccountRepository.findByAccountNumber(toAccountNumber)).thenThrow(givenException);

            //when & then
            var exception = assertThrows(
                RuntimeException.class,
                () -> service.transferFunds(fromAccountNumber, toAccountNumber, transferAmount)
            );

            assertSame(givenException, exception);
            verify(bankAccountRepository).findByAccountNumber(fromAccountNumber);
            verify(bankAccountRepository).findByAccountNumber(toAccountNumber);
            verifyNoInteractions(transferExecutor);
        }
    }

    private static final class Fixtures {
        private static final AccountNumber VALID_ACCOUNT_NUMBER_A =
            new AccountNumber("00 1111 2222 3333 4444 5555 6666");
        private static final AccountNumber VALID_ACCOUNT_NUMBER_B =
            new AccountNumber("01 1111 2222 3333 4444 5555 6666");
    }
}

package pl.eked.lab.banking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AccountNumberTest {

    @Nested
    class Validation {
        @Test
        void shouldCreateAccountNumberWhenFormatIsValid() {
            //given
            var givenAccountNumber = Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER;

            //when
            var result = new AccountNumber(givenAccountNumber);

            //then
            assertEquals(givenAccountNumber, result.value());
        }

        @Test
        void shouldRejectNullAccountNumber() {
            // given
            String givenAccountNumber = null;
            var expectedErrorMsg = "Account number cannot be null or empty";

            //when & then
            var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AccountNumber(givenAccountNumber)
            );

            assertEquals(expectedErrorMsg, exception.getMessage());
        }

        @Test
        void shouldRejectEmptyAccountNumber() {
            // given
            var givenAccountNumber = "";
            var expectedErrorMsg = "Account number cannot be null or empty";

            //when & then
            var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AccountNumber(givenAccountNumber)
            );

            assertEquals(expectedErrorMsg, exception.getMessage());
        }

        @Test
        void shouldRejectWhitespaceOnlyAccountNumber() {
            //given
            var givenAccountNumber = Fixtures.INVALID_ACCOUNT_NUMBER_HAS_ONLY_WHITESPACES;
            var expectedErrorMsg = "Invalid account number, must contain exactly 26 digits";

            //when & then
            var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AccountNumber(givenAccountNumber)
            );

            assertEquals(expectedErrorMsg, exception.getMessage());
        }

        @Test
        void shouldRejectAccountNumberWithInvalidLength() {
            // given
            var givenAccountNumber = Fixtures.INVALID_ACCOUNT_NUMBER_ONLY_25_DIGITS;
            var expectedErrorMsg = "Invalid account number, must contain exactly 26 digits";

            //when & then
            var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AccountNumber(givenAccountNumber)
            );

            assertEquals(expectedErrorMsg, exception.getMessage());
        }

        @Test
        void shouldRejectAccountNumberContainingNonDigitCharacters() {
            // given
            var givenAccountNumber = Fixtures.INVALID_ACCOUNT_NUMBER_HAS_ONE_LETTER;
            var expectedErrorMsg = "Invalid account number, must contain exactly 26 digits";

            //when & then
            var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AccountNumber(givenAccountNumber)
            );

            assertEquals(expectedErrorMsg, exception.getMessage());
        }
    }

    @Nested
    class Normalization {
        @Test
        void shouldNormalizeAccountNumberContainingSpaces() {
            //given
            var givenAccountNumber = Fixtures.UNNORMALIZED_TYPICAL_VALID_ACCOUNT_NUMBER;
            var expectedAccountNumber = Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER;

            //when
            var result = new AccountNumber(givenAccountNumber);

            //then
            assertEquals(expectedAccountNumber, result.value());
        }

        @Test
        void shouldNormalizeAccountNumberContainingWhitespaceCharacters() {
            //given
            var givenAccountNumber = Fixtures.UNNORMALIZED_WITH_MANY_WHITESPACES_VALID_ACCOUNT_NUMBER;
            var expectedAccountNumber = Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER;

            //when
            var result = new AccountNumber(givenAccountNumber);

            //then
            assertEquals(expectedAccountNumber, result.value());
        }

        @Test
        void shouldConsiderAccountNumbersEqualRegardlessOfFormatting() {
            //given
            var givenAccountNumber1 = Fixtures.UNNORMALIZED_TYPICAL_VALID_ACCOUNT_NUMBER;
            var givenAccountNumber2 = Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER;
            var accountNumber1 = new AccountNumber(givenAccountNumber1);
            var accountNumber2 = new AccountNumber(givenAccountNumber2);

            //when
            var result = accountNumber1.equals(accountNumber2);

            //then
            assertTrue(result);
        }

        @Test
        void shouldGenerateSameHashCodeForEqualAccountNumbers() {
            //given
            var givenAccountNumber1 = Fixtures.UNNORMALIZED_TYPICAL_VALID_ACCOUNT_NUMBER;
            var givenAccountNumber2 = Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER;
            var accountNumber1 = new AccountNumber(givenAccountNumber1);
            var accountNumber2 = new AccountNumber(givenAccountNumber2);

            //when
            var result1 = accountNumber1.hashCode();
            var result2 = accountNumber2.hashCode();

            //then
            assertEquals(result1, result2);
        }
    }

    @Nested
    class Comparison {
        @Test
        void shouldReturnNegativeValueWhenAccountNumberIsSmaller() {
            //given
            var givenAccountNumber1 = Fixtures.ACCOUNT_NUMBER_SMALLER;
            var givenAccountNumber2 = Fixtures.ACCOUNT_NUMBER_GREATER;
            var accountNumber1 = new AccountNumber(givenAccountNumber1);
            var accountNumber2 = new AccountNumber(givenAccountNumber2);

            //when
            var result =  accountNumber1.compareTo(accountNumber2);
            var isSmaller = result < 0;

            //then
            assertTrue(isSmaller);
        }

        @Test
        void shouldReturnPositiveValueWhenAccountNumberIsGreater() {
            //given
            var givenAccountNumber1 = Fixtures.ACCOUNT_NUMBER_SMALLER;
            var givenAccountNumber2 = Fixtures.ACCOUNT_NUMBER_GREATER;
            var accountNumber1 = new AccountNumber(givenAccountNumber1);
            var accountNumber2 = new AccountNumber(givenAccountNumber2);

            //when
            var result =  accountNumber2.compareTo(accountNumber1);
            var isGreater = result > 0;

            //then
            assertTrue(isGreater);
        }

        @Test
        void shouldReturnZeroWhenAccountNumbersAreEqual() {
            //given
            var givenAccountNumber1 = Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER;
            var givenAccountNumber2 = Fixtures.NORMALIZED_VALID_ACCOUNT_NUMBER;
            var accountNumber1 = new AccountNumber(givenAccountNumber1);
            var accountNumber2 = new AccountNumber(givenAccountNumber2);

            //when
            var result =  accountNumber1.compareTo(accountNumber2);
            var isTheSame = result == 0;

            //then
            assertTrue(isTheSame);
        }
    }

    private static final class Fixtures {
        private static final String UNNORMALIZED_TYPICAL_VALID_ACCOUNT_NUMBER = "12 3456 7890 1234 5678 9012 3456";
        private static final String UNNORMALIZED_WITH_MANY_WHITESPACES_VALID_ACCOUNT_NUMBER = "         12 3456 7890  \t  1234 5678      9012 3456    \n\t";
        private static final String NORMALIZED_VALID_ACCOUNT_NUMBER = "12345678901234567890123456";
        private static final String INVALID_ACCOUNT_NUMBER_ONLY_25_DIGITS = "1234567890123456789012345";
        private static final String INVALID_ACCOUNT_NUMBER_HAS_ONE_LETTER = "P1234567890123456789012345";
        private static final String INVALID_ACCOUNT_NUMBER_HAS_ONLY_WHITESPACES = "          \t                   \n\t";
        private static final String ACCOUNT_NUMBER_SMALLER = "00 1111 2222 3333 4444 5555 6666";
        private static final String ACCOUNT_NUMBER_GREATER = "00 1111 2222 3333 4444 5555 6667";
    }
}
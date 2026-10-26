package pl.eked.lab.banking.domain;

public record AccountNumber(String value) implements Comparable<AccountNumber> {
    private static final String ACCOUNT_NUMBER_REGEX = "^\\d{26}$";

    public AccountNumber {
        validateNotNullOrEmpty(value);
        var normalized = normalize(value);
        validateFormat(normalized);
        value = normalized;
    }

    @Override
    public int compareTo(AccountNumber other) {
        return this.value.compareTo(other.value);
    }

    private static void validateNotNullOrEmpty(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
    }

    private static void validateFormat(String accountNumber) {
        if (!accountNumber.matches(ACCOUNT_NUMBER_REGEX))
            throw new IllegalArgumentException("Invalid account number, must contain exactly 26 digits");
    }

    private static String normalize(String accountNumber) {
        return accountNumber.replaceAll("\\s", "");
    }
}

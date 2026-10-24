package pl.eked.lab.banking;

public class AccountNumberNormalizer {
    private AccountNumberNormalizer() {
        throw new IllegalAccessError("Utility class");
    }

    public static String normalize(String accountNumber) {
        return accountNumber != null ? accountNumber.replace(" ", "") : null;
    }
}

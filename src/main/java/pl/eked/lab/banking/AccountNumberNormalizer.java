package pl.eked.lab.banking;

public class AccountNumberNormalizer {
    private AccountNumberNormalizer() {}

    public static String normalize(String accountNumber) {
        return accountNumber != null ? accountNumber.replace(" ", "") : null;
    }
}

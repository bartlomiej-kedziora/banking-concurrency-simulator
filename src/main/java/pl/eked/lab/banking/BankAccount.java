package pl.eked.lab.banking;

public class BankAccount {
    private static final String ACCOUNT_NUMBER_REGEX = "^\\d{26}$";
    private final String accountNumber;
    private int balance;

    public BankAccount(String accountNumber, int balance) {
        var normalizedAccountNumber = AccountNumberNormalizer.normalize(accountNumber);
        this.validateAccountNumber(normalizedAccountNumber);
        this.validateBalance(balance);

        this.accountNumber = normalizedAccountNumber;
        this.balance = balance;
    }

    public synchronized boolean deposit(int amount) {
        if (!canDeposit(amount))
            return false;

        balance += amount;

        return true;
    }

    public synchronized boolean canDeposit(int amount) {
        return amount > 0 && amount <= Integer.MAX_VALUE - balance;
    }

    public synchronized boolean withdraw(int amount) {
        if (!canWithdraw(amount))
            return false;

        this.balance -= amount;

        return true;
    }

    public synchronized boolean canWithdraw(int amount) {
        if (amount <= 0)
            return false;

        return amount <= balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public synchronized int getBalance() {
        return balance;
    }

    private void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || !accountNumber.matches(ACCOUNT_NUMBER_REGEX))
            throw new IllegalArgumentException("Invalid account number, should contains 26 digits");
    }

    private void validateBalance(int balance) {
        if (balance < 0)
            throw new IllegalArgumentException("Invalid balance");
    }
}

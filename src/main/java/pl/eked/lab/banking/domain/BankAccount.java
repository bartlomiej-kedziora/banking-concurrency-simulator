package pl.eked.lab.banking.domain;

public class BankAccount {
    private final AccountNumber accountNumber;
    private int balance;

    public BankAccount(AccountNumber accountNumber, int balance) {
        this.validateAccountNumberNotNull(accountNumber);
        this.validateBalance(balance);
        this.accountNumber = accountNumber;
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

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public synchronized int getBalance() {
        return balance;
    }

    private void validateAccountNumberNotNull(AccountNumber accountNumber) {
        if (accountNumber == null)
            throw new NullPointerException("Account number cannot be null");
    }

    private void validateBalance(int balance) {
        if (balance < 0)
            throw new IllegalArgumentException("Invalid balance");
    }
}

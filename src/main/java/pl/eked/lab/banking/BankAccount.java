package pl.eked.lab.banking;

public class BankAccount {
    private int balance;

    public BankAccount(int balance) {
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

    public synchronized int getBalance() {
        return balance;
    }
}

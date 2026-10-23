package pl.eked.lab.banking;

public class BankAccount {
    private int balance;

    public BankAccount(int balance) {
        this.balance = balance;
    }

    public synchronized boolean withdraw(int amount) {
        if (amount < 0) {
            return false;
        }

        if (amount > balance) {
            return false;
        }

        this.balance = this.balance - amount;

        return true;
    }

    public synchronized int getBalance() {
        return balance;
    }
}

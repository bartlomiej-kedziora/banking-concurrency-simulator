package pl.eked.lab.banking;

public class BankAccount {
    private final int balance;

    public BankAccount(int balance) {
        this.balance = balance;
    }

    public boolean withdraw(int amount) {
        if (amount < 0) {
            return false;
        }

        if (amount > balance) {
            return false;
        }

        return true;
    }

    public int getBalance() {
        return balance;
    }
}

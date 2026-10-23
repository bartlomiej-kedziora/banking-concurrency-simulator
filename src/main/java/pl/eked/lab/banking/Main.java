package pl.eked.lab.banking;

public class Main {

    static void main() throws InterruptedException {
        BankAccount bankAccount1 = new BankAccount(1000);

        Thread threadA = new Thread(new BankAccountWithdrawTask(bankAccount1, 700));
        Thread threadB = new Thread(new BankAccountWithdrawTask(bankAccount1, 500));

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();

        System.out.println("Balance after withdraw operations: " + bankAccount1.getBalance());
    }

    private static class BankAccountWithdrawTask implements Runnable {
        private final BankAccount bankAccount;
        private final int amount;

        public BankAccountWithdrawTask(BankAccount bankAccount, int amount) {
            this.bankAccount = bankAccount;
            this.amount = amount;
        }

        @Override
        public void run() {
            var withdrawResult = bankAccount.withdraw(amount) ? "Accepted" : "Rejected";
            System.out.println(Thread.currentThread().getName() + " withdraw result: " + withdrawResult);
        }
    }
}

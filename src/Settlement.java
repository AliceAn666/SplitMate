import java.util.ArrayList;
public class Settlement {

    // Calculate each person's balance and suggest transfers.
    public static void settlePayments(
            ArrayList<Participant> participants,
            ArrayList<Expense> expenses) {
        if (participants.isEmpty()) {
            System.out.println("No participants yet.");
            return;
        }
        if (expenses.isEmpty()) {
            System.out.println("No expenses yet.");
            return;
        }
        int count = participants.size();
        long[] balances = new long[count];
        System.out.println("\n--- Balances ---");

        // Calculate each person's net balance.
        for (int i = 0; i < count; i++) {
            Participant person = participants.get(i);
            long paid = PaymentTracker.calculatePaid(
                    person.getId(), expenses);
            long owed = PaymentTracker.calculateOwed(
                    person.getId(), expenses);
            balances[i] = paid - owed;
            System.out.printf(
                    "%s | Balance: $%.2f%n",
                    person.getName(),
                    balances[i] / 100.0);
        }
        System.out.println("\n--- Settlement Results ---");
        boolean hasTransfers = false;

        // Match people who owe money with people who should receive money.
        for (int i = 0; i < count; i++) {
            if (balances[i] >= 0) {
                continue;
            }
            for (int j = 0; j < count; j++) {
                if (balances[i] == 0) {
                    break;
                }
                if (balances[j] <= 0) {
                    continue;
                }
                long amount = Math.min(
                        -balances[i], balances[j]);
                System.out.printf(
                        "%s pays %s: $%.2f%n",
                        participants.get(i).getName(),
                        participants.get(j).getName(),
                        amount / 100.0);
                balances[i] += amount;
                balances[j] -= amount;
                hasTransfers = true;
            }
        }
        if (!hasTransfers) {
            System.out.println("No transfers needed.");
        }
    }
}
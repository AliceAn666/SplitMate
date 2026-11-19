import java.util.ArrayList;
public class Settlement {

    // Calculate balances and show the payments needed.
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

        // Store each person's balance in cents.
        long[] balances = new long[count];
        long totalBalance = 0;

        // Calculate each person's balance.
        for (int i = 0; i < count; i++) {
            int id = participants.get(i).getId();
            long paid = PaymentTracker.calculatePaid(
                    id, expenses);
            long owed = PaymentTracker.calculateOwed(
                    id, expenses);
            balances[i] = paid - owed;
            totalBalance += balances[i];
        }

        // All balances must add up to zero.
        if (totalBalance != 0) {
            System.out.println(
                    "Paid and owed totals do not match. Check expense data.");
            return;
        }

        // Display balances.
        System.out.println("\n--- Balances ---");
        for (int i = 0; i < count; i++) {
            System.out.printf(
                    "%s | Balance: $%.2f%n",
                    participants.get(i).getName(),
                    balances[i] / 100.0);
        }

        // Display settlement results.
        System.out.println("\n--- Settlement Results ---");
        boolean hasTransfers = false;

        // Find participants who owe money.
        for (int i = 0; i < count; i++) {
            if (balances[i] >= 0) {
                continue;
            }

            // Find participants who should receive money.
            for (int j = 0; j < count; j++) {
                if (balances[i] == 0) {
                    break;
                }
                if (balances[j] <= 0) {
                    continue;
                }

                // Transfer the smaller amount.
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
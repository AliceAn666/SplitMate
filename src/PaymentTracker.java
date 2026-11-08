import java.util.ArrayList;

public class PaymentTracker {

    // Calculate how much this person has paid, in cents.
    public static long calculatePaid(
            int id, ArrayList<Expense> expenses) {

        long paid = 0;

        for (Expense expense : expenses) {
            if (expense.getPayerId() == id) {
                paid += Math.round(expense.getAmount() * 100);
            }
        }

        return paid;
    }

    // Calculate this person's total share, in cents.
    public static long calculateOwed(
            int id, ArrayList<Expense> expenses) {

        long owed = 0;

        for (Expense expense : expenses) {
            ArrayList<Integer> sharedIds = expense.getSharedIds();

            long[] shares = BillCalculator.splitSharedExpense(
                    expense.getAmount(), sharedIds);

            for (int i = 0; i < sharedIds.size(); i++) {
                if (sharedIds.get(i).intValue() == id) {
                    owed += shares[i];
                }
            }
        }

        return owed;
    }

    // Display each person's paid and owed amounts.
    public static void viewPayments(
            ArrayList<Participant> participants,
            ArrayList<Expense> expenses) {

        if (participants.isEmpty()) {
            System.out.println("No participants yet.");
            return;
        }

        System.out.println("\n--- Payments ---");

        for (Participant person : participants) {
            long paid = calculatePaid(
                    person.getId(), expenses);

            long owed = calculateOwed(
                    person.getId(), expenses);

            System.out.printf(
                    "%s | Paid: $%.2f | Owed: $%.2f%n",
                    person.getName(),
                    paid / 100.0,
                    owed / 100.0);
        }
    }
}
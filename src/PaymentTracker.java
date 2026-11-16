import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // Calculate this person's total owed in dollars, keeping precision.
    public static BigDecimal calculateOwedAmount(
            int id, ArrayList<Expense> expenses) {

        BigDecimal owed = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            ArrayList<Integer> sharedIds = expense.getSharedIds();
            int position = sharedIds.indexOf(id);

            if (position < 0) {
                continue;
            }

            ArrayList<BigDecimal> owedAmounts = expense.getOwedAmounts();

            if (owedAmounts.isEmpty()) {
                long[] shares = BillCalculator.splitSharedExpense(
                        expense.getAmount(), sharedIds);

                owed = owed.add(
                        BigDecimal.valueOf(shares[position], 2));
            } else {
                owed = owed.add(owedAmounts.get(position));
            }
        }

        return owed;
    }

    // Return cents for existing callers.
    public static long calculateOwed(
            int id, ArrayList<Expense> expenses) {

        return calculateOwedAmount(id, expenses)
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
    }

    // Display paid amounts and high-precision owed amounts.
    public static void viewPayments(
            ArrayList<Participant> participants,
            ArrayList<Expense> expenses) {

        if (participants.isEmpty()) {
            System.out.println("No participants yet.");
            return;
        }

        System.out.println("\n--- Payments ---");

        for (Participant person : participants) {
            long paid = calculatePaid(person.getId(), expenses);
            BigDecimal owed = calculateOwedAmount(
                    person.getId(), expenses);

            System.out.printf(
                    "%s | Paid: $%.2f | Owed: $%.3f%n",
                    person.getName(),
                    paid / 100.0,
                    owed);
        }
    }
}
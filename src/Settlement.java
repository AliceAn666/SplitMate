import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class Settlement {

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
        BigDecimal[] fractions = new BigDecimal[count];
        BigDecimal totalBalance = BigDecimal.ZERO;
        long roundedTotal = 0;

        // Calculate precise balances before converting to whole cents.
        for (int i = 0; i < count; i++) {
            int id = participants.get(i).getId();

            BigDecimal paid = BigDecimal.valueOf(
                    PaymentTracker.calculatePaid(id, expenses), 2);
            BigDecimal owed = PaymentTracker.calculateOwedAmount(
                    id, expenses);
            BigDecimal balance = paid.subtract(owed);

            totalBalance = totalBalance.add(balance);

            BigDecimal cents = balance.movePointRight(2);
            BigDecimal wholeCents = cents.setScale(0, RoundingMode.FLOOR);

            balances[i] = wholeCents.longValueExact();
            fractions[i] = cents.subtract(wholeCents);
            roundedTotal = Math.addExact(roundedTotal, balances[i]);
        }

        // Allow tiny decimal division differences, and detect inconsistent data.
        if (totalBalance.abs().compareTo(
                new BigDecimal("0.00000001")) > 0) {
            System.out.println(
                    "Paid and owed totals do not match. Check expense data.");
            return;
        }

        long centsToAdjust = -roundedTotal;
        if (centsToAdjust < 0 || centsToAdjust > count) {
            System.out.println("Unable to balance settlement amounts.");
            return;
        }

        // Round the balances together so their sum is exactly zero.
        for (long step = 0; step < centsToAdjust; step++) {
            int best = 0;

            for (int i = 1; i < count; i++) {
                if (fractions[i].compareTo(fractions[best]) > 0) {
                    best = i;
                }
            }

            balances[best]++;
            fractions[best] = BigDecimal.valueOf(-1);
        }

        System.out.println("\n--- Balances ---");
        for (int i = 0; i < count; i++) {
            System.out.printf(
                    "%s | Balance: $%.2f%n",
                    participants.get(i).getName(),
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

                long amount = Math.min(-balances[i], balances[j]);

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
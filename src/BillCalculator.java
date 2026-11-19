import java.util.ArrayList;
public class BillCalculator {

    // Calculate tax using a percentage.
    public static double calculateTax(double amount, double taxRate) {
        return amount * taxRate / 100;
    }

    // Calculate tip using a percentage.
    public static double calculateTip(double amount, double tipRate) {
        return amount * tipRate / 100;
    }

    // Calculate discount using a percentage.
    public static double calculateDiscount(double amount, double discountRate) {
        return amount * discountRate / 100;
    }

    // Convert dollars to cents.
    public static long toCents(double amount) {
        return Math.round(amount * 100);
    }

    // Divide an amount equally among participants.
    public static long[] splitEqually(double amount, int count) {
        if (count <= 0 || !Double.isFinite(amount)
                || amount < 0 || amount > 1000000000) {
            throw new IllegalArgumentException("Invalid amount or count.");
        }
        long totalCents = toCents(amount);
        long[] shares = new long[count];
        long base = totalCents / count;
        long remainder = totalCents % count;
        for (int i = 0; i < count; i++) {
            shares[i] = base;
            if (i < remainder) {
                shares[i]++;
            }
        }
        return shares;
    }

    // Divide an expense among selected participants.
    public static long[] splitSharedExpense(
            double amount, ArrayList<Integer> sharedIds) {
        return splitEqually(amount, sharedIds.size());
    }

    // Add one item's cost to each selected person's food total.
    public static void addItemToFoodTotals(
            double itemPrice,
            ArrayList<Integer> itemSharedIds,
            ArrayList<Integer> dinerIds,
            long[] foodTotals) {
        if (!Double.isFinite(itemPrice)
                || itemPrice < 0 || itemPrice > 1000000000) {
            throw new IllegalArgumentException("Invalid item price.");
        }
        if (itemSharedIds.isEmpty()
                || foodTotals.length != dinerIds.size()) {
            throw new IllegalArgumentException("Invalid participant list.");
        }

        // Check all participant IDs before changing totals.
        for (int i = 0; i < itemSharedIds.size(); i++) {
            int id = itemSharedIds.get(i);
            if (!dinerIds.contains(id)
                    || itemSharedIds.indexOf(id) != i) {
                throw new IllegalArgumentException("Invalid participant ID.");
            }
        }
        long[] shares = splitEqually(
                itemPrice, itemSharedIds.size());
        for (int i = 0; i < itemSharedIds.size(); i++) {
            int id = itemSharedIds.get(i);
            int position = dinerIds.indexOf(id);
            foodTotals[position] += shares[i];
        }
    }

    // Divide tax or discount based on each person's food amount.
    public static long[] splitProportionally(
            long amountCents, long[] foodCents) {
        if (amountCents < 0) {
            throw new IllegalArgumentException("Invalid amount.");
        }
        long totalFood = 0;
        for (int i = 0; i < foodCents.length; i++) {
            if (foodCents[i] < 0) {
                throw new IllegalArgumentException("Invalid food amount.");
            }
            totalFood += foodCents[i];
        }
        long[] shares = new long[foodCents.length];
        if (amountCents == 0) {
            return shares;
        }
        if (totalFood == 0) {
            throw new IllegalArgumentException("No food amount to split.");
        }
        long remaining = amountCents;

        // Calculate each person's proportional share.
        for (int i = 0; i < foodCents.length; i++) {
            double ratio = (double) foodCents[i] / totalFood;
            shares[i] = (long) (amountCents * ratio);
            remaining -= shares[i];
        }

        // Distribute any remaining cents.
        int index = 0;
        while (remaining > 0) {
            if (foodCents[index] > 0) {
                shares[index]++;
                remaining--;
            }
            index++;
            if (index == foodCents.length) {
                index = 0;
            }
        }
        return shares;
    }
}
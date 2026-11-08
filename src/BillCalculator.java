import java.util.ArrayList;
public class BillCalculator {
    public static double calculateTax(double amount, double taxRate) {
        double tax = amount * taxRate / 100;
        return tax;
    }
    public static double calculateTip(double amount, double tipRate) {
        double tip = amount * tipRate / 100;
        return tip;
    }
    public static double calculateDiscount(double amount, double discountRate) {
        double discount = amount * discountRate / 100;
        return discount;
    }
    // Split the amount in cents to keep the total correct.
    public static long[] splitEqually(double amount, int count) {
        if (count <= 0 || !Double.isFinite(amount)
                || amount < 0 || amount > 1000000000) {
            throw new IllegalArgumentException(
                    "Invalid amount or participant count.");
        }

        long cents = Math.round(amount * 100);
        long[] shares = new long[count];

        long base = cents / count;
        long extra = cents % count;

        for (int i = 0; i < count; i++) {
            shares[i] = base;

            if (i < extra) {
                shares[i]++;
            }
        }

        return shares;
    }

    // Split among the selected people in the order they were added.
    public static long[] splitSharedExpense(
            double amount, ArrayList<Integer> sharedIds) {

        return splitEqually(amount, sharedIds.size());
    }
}

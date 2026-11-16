import java.math.BigDecimal;
import java.math.MathContext;
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
    
    // Add this item's share to each diner's food subtotal, in dollars.
    public static void addItemToFoodTotals(
            double itemPrice,
            ArrayList<Integer> itemSharedIds,
            ArrayList<Integer> dinerIds,
            BigDecimal[] foodTotals) {

        if (!Double.isFinite(itemPrice) || itemPrice < 0 || itemPrice > 1000000000) {
            throw new IllegalArgumentException("Invalid item price.");
        }

        if (itemSharedIds.isEmpty() || foodTotals.length != dinerIds.size()) {
            throw new IllegalArgumentException("Invalid diner list.");
        }

        int[] positions = new int[itemSharedIds.size()];

        for (int i = 0; i < itemSharedIds.size(); i++) {
            Integer id = itemSharedIds.get(i);
            int position = dinerIds.indexOf(id);

            if (position < 0 || itemSharedIds.indexOf(id) != i) {
                throw new IllegalArgumentException(
                        "Unknown or repeated participant: " + id);
            }

            positions[i] = position;
        }

        BigDecimal price = BigDecimal.valueOf(itemPrice);
        BigDecimal count = BigDecimal.valueOf(itemSharedIds.size());
        BigDecimal share = price.divide(count, MathContext.DECIMAL128);

        for (int position : positions) {
            foodTotals[position] = foodTotals[position].add(share);
        }
    }
    
    // Split tax or discount in proportion to each diner's food amount.
    public static long[] splitProportionally(long amountCents, long[] foodCents) {
        if (amountCents < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }

        long totalFood = 0;
        for (long food : foodCents) {
            if (food < 0) {
                throw new IllegalArgumentException("Food amount cannot be negative.");
            }
            totalFood = Math.addExact(totalFood, food);
        }

        long[] shares = new long[foodCents.length];
        if (amountCents == 0) {
            return shares;
        }
        if (totalFood == 0) {
            throw new IllegalArgumentException("No food amount to split by.");
        }

        // Use exact arithmetic even when two large cent amounts are multiplied.
        java.math.BigInteger amount = java.math.BigInteger.valueOf(amountCents);
        java.math.BigInteger total = java.math.BigInteger.valueOf(totalFood);
        long[] remainders = new long[foodCents.length];
        long centsLeft = amountCents;

        for (int i = 0; i < foodCents.length; i++) {
            java.math.BigInteger weight = java.math.BigInteger.valueOf(foodCents[i]);
            java.math.BigInteger[] result =
                    amount.multiply(weight).divideAndRemainder(total);

            shares[i] = result[0].longValueExact();
            remainders[i] = result[1].longValueExact();
            centsLeft -= shares[i];
        }

        // Give each leftover cent to the diner with the largest remainder.
        while (centsLeft > 0) {
            int best = 0;
            for (int i = 1; i < remainders.length; i++) {
                if (remainders[i] > remainders[best]) {
                    best = i;
                }
            }
            shares[best]++;
            remainders[best] = -1;
            centsLeft--;
        }

        return shares;
    }
    
    // Split a receipt amount in proportion to each person's subtotal.
    public static BigDecimal[] splitProportionally(
            BigDecimal amount, BigDecimal[] subtotals) {

        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal subtotal : subtotals) {
            total = total.add(subtotal);
        }

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
        if (amount.signum() > 0 && total.signum() == 0) {
            throw new IllegalArgumentException("No subtotal to split this amount by.");
        }

        BigDecimal[] shares = new BigDecimal[subtotals.length];
        for (int i = 0; i < subtotals.length; i++) {
            if (amount.signum() == 0) {
                shares[i] = BigDecimal.ZERO;
            } else {
                shares[i] = amount.multiply(subtotals[i])
                        .divide(total, MathContext.DECIMAL128);
            }
        }
        return shares;
    }
}
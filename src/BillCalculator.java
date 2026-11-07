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
}

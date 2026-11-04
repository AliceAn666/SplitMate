public class Expense {
    private String description;
    private double amount;
    private int payerId;
    public Expense(String description, double amount, int payerId) {
        this.description = description;
        this.amount = amount;
        this.payerId = payerId;
    }
    public String getDescription() {
        return description;
    }
    public double getAmount() {
        return amount;
    }
    public int getId(){
        return payerId;
    }
}

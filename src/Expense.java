public class Expense {
    private String description;
    private double amount;
    private int payerId;
    private String date;
    public Expense(String description, double amount, int payerId,String date) {
        this.description = description;
        this.amount = amount;
        this.payerId = payerId;
        this.date = date;
    }
    public String getDescription() {
        return description;
    }
    public double getAmount() {
        return amount;
    }
    public int getPayerId(){
        return payerId;
    }
    public String getDate(){
        return date;
    }
}

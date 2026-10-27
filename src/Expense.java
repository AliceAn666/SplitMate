public class Expense {
    private String description;
    private double amount;
    private int Id;
    public Expense(String description, double amount, int Id) {
        this.description = description;
        this.amount = amount;
        this.Id = Id;
    }
    public String getDescription() {
        return description;
    }
    public double getAmount() {
        return amount;
    }
    public int getId(){
        return Id;
    }
}

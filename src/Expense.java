import java.util.ArrayList;
public class Expense {
    private String description;
    private double amount;
    private int payerId;
    private String date;
    private ArrayList<Integer>sharedIds;
    public Expense(String description, double amount, int payerId,String date, ArrayList<Integer>sharedIds) {
        this.description = description;
        this.amount = amount;
        this.payerId = payerId;
        this.date = date;
        this.sharedIds = new ArrayList<>(sharedIds);
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
    public ArrayList<Integer> getSharedIds() {
        return new ArrayList<>(sharedIds);
    }
}

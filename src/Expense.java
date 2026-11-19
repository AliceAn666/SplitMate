import java.util.ArrayList;
public class Expense {
    private String description;
    private double amount;
    private int payerId;
    private String date;
    private ArrayList<Integer> sharedIds;
    private ArrayList<Long> owedAmounts;

    // Constructor for equal and selected-participant splits.
    public Expense(String description, double amount, int payerId,
            String date, ArrayList<Integer> sharedIds) {

        this.description = description;
        this.amount = amount;
        this.payerId = payerId;
        this.date = date;
        this.sharedIds = new ArrayList<>(sharedIds);
        this.owedAmounts = new ArrayList<>();
    }

    // Constructor for item-based splits.
    public Expense(String description, double amount, int payerId,
            String date, ArrayList<Integer> sharedIds,
            ArrayList<Long> owedAmounts) {

        this(description, amount, payerId, date, sharedIds);
        if (sharedIds.size() != owedAmounts.size()) {
            throw new IllegalArgumentException(
                    "Each participant must have one owed amount.");
        }
        for (long owed : owedAmounts) {
            if (owed < 0) {
                throw new IllegalArgumentException(
                    "Owed amounts cannot be negative.");
            }
        }
        this.owedAmounts = new ArrayList<>(owedAmounts);
    }
    public String getDescription() {
        return description;
    }
    public double getAmount() {
        return amount;
    }
    public int getPayerId() {
        return payerId;
    }
    public String getDate() {
        return date;
    }
    public ArrayList<Integer> getSharedIds() {
        return new ArrayList<>(sharedIds);
    }
    public ArrayList<Long> getOwedAmounts() {
        return new ArrayList<>(owedAmounts);
    }
}
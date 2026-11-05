import java.util.ArrayList;
import java.util.Scanner;
public class ExpenseManager {
    public static void addExpense(Scanner input, ArrayList<Expense> expenses, ArrayList<Participant> participants){
        System.out.println("Enter the name of the expense: ");
        String name = input.nextLine();
        System.out.println("Enter amount: ");
        double amount = input.nextDouble();
        input.nextLine();
        System.out.println("Enter date: ");
        String date = input.nextLine();
        System.out.print("Enter payer name: ");
        String payerName = input.nextLine().trim();
        int payerId = -1;
        for (Participant person : participants) {
            if (person.getName().equalsIgnoreCase(payerName)) {
                payerId = person.getId();
                break;
            }
        }
        if (payerId == -1) {
            System.out.println("Participant not found.");
            return;
        }
        Expense expense = new Expense(name, amount, payerId, date);
        expenses.add(expense);
    }
    public static void viewExpense(ArrayList<Expense> expenses, ArrayList<Participant> participants){
        if(expenses.isEmpty()){
            System.out.println("There are no expenses to view");
            return;
        }
        for(Expense expense: expenses){
            System.out.println("Name: " + expense.getDescription());
            System.out.println("Amount: " + expense.getAmount());
            System.out.println("Date: " + expense.getDate());
            String payerName = "Unknown";
            for (Participant person : participants) {
                if (person.getId() == expense.getPayerId()) {
                    payerName = person.getName();
                    break;
                }
            }
            System.out.println("Payer: " + payerName);
            System.out.println();
        }
    }
}

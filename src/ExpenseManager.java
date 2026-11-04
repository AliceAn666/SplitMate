import java.util.ArrayList;
import java.util.Scanner;
public class ExpenseManager {
    public static void addExpense(Scanner input, ArrayList<Expense> expenses){
        System.out.println("Enter the name of the expense: ");
        String name = input.nextLine();
        System.out.println("Enter amount: ");
        double amount = input.nextDouble();
        input.nextLine();
        System.out.println("Enter date: ");
        String date = input.nextLine();
        System.out.print("Enter payerId: ");
        int payerId = input.nextInt();
        input.nextLine();
        Expense expense = new Expense(name, amount, payerId, date);
        expenses.add(expense);
    }
    public static void viewExpense(ArrayList<Expense> expenses){
        if(expenses.isEmpty()){
            System.out.println("There are no expenses to view");
            return;
        }
        for(Expense expense: expenses){
            System.out.println("Name: " + expense.getDescription());
            System.out.println("Amount: " + expense.getAmount());
            System.out.println("Date: " + expense.getDate());
            System.out.println("Payer ID: " + expense.getPayerId());
            System.out.println();
        }
    }
}

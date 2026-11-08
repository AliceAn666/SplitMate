import java.util.ArrayList;
import java.util.Scanner;
public class ExpenseManager {
    public static void addExpense(Scanner input, ArrayList<Expense> expenses, ArrayList<Participant> participants){
    if(participants.isEmpty()){
            System.out.println("Add participant first.");
            return;
    }
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
        ArrayList<Integer> sharedIds = new ArrayList<>();

        System.out.println("1. Split among all participants");
        System.out.println("2. Split among selected participants");
        System.out.print("Choose split type: ");
        String choice = input.nextLine().trim();

        if (choice.equals("1")) {
            for (Participant person : participants) {
                sharedIds.add(person.getId());
            }
        } else if (choice.equals("2")) {
            System.out.print("How many participants will share this expense? ");

            if (!input.hasNextInt()) {
                input.nextLine();
                System.out.println("Please enter a whole number.");
                return;
            }

            int count = input.nextInt();
            input.nextLine();

            if (count < 1 || count > participants.size()) {
                System.out.println("Invalid number of participants.");
                return;
            }

            while (sharedIds.size() < count) {
                System.out.print("Enter participant name: ");
                String sharedName = input.nextLine().trim();
                int sharedId = -1;

                for (Participant person : participants) {
                    if (person.getName().equalsIgnoreCase(sharedName)) {
                        sharedId = person.getId();
                        break;
                    }
                }

                if (sharedId == -1) {
                    System.out.println("Participant not found.");
                } else if (sharedIds.contains(sharedId)) {
                    System.out.println("This participant is already selected.");
                } else {
                    sharedIds.add(sharedId);
                }
            }
        } else {
            System.out.println("Invalid choice. Expense was not added.");
            return;
        }
        Expense expense = new Expense(name, amount, payerId, date,sharedIds);
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
            ArrayList<Integer> sharedIds = expense.getSharedIds();

            long[] shares = BillCalculator.splitSharedExpense(
                    expense.getAmount(), sharedIds);

            for (int i = 0; i < sharedIds.size(); i++) {
                String sharedName = "Unknown";

                for (Participant person : participants) {
                    if (person.getId() == sharedIds.get(i).intValue()) {
                        sharedName = person.getName();
                        break;
                    }
                }

                System.out.printf(
                        "%s owes: $%.2f%n",
                        sharedName,
                        shares[i] / 100.0);
            }
            System.out.println();
        }
    }
}

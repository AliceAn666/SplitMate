import java.util.ArrayList;
import java.util.Scanner;
public class ExpenseManager {

    // Read a dollar amount and convert it to cents.
    private static long readCents(Scanner input) {
        String text = input.nextLine().trim();
        try {
            double amount = Double.parseDouble(text);
            if (!Double.isFinite(amount)
                    || amount < 0 || amount > 1000000000) {
                return -1;
            }
            double cents = amount * 100;
            long rounded = Math.round(cents);
            if (Math.abs(cents - rounded) > 0.000001) {
                return -1;
            }
            return rounded;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Keep asking until the user enters a valid amount.
    private static long askAmount(Scanner input, String message) {
        while (true) {
            System.out.print(message);
            long cents = readCents(input);
            if (cents >= 0) {
                return cents;
            }
            System.out.println(
                    "Please enter a valid amount with at most two decimal places.");
        }
    }

    // Find a participant by name.
    private static int findParticipant(
            String name, ArrayList<Participant> participants) {
        for (Participant person : participants) {
            if (person.getName().equalsIgnoreCase(name)) {
                return person.getId();
            }
        }
        return -1;
    }

    // Ask a yes or no question.
    private static boolean askYesNo(Scanner input, String message) {
        while (true) {
            System.out.print(message);
            String answer = input.nextLine().trim();
            if (answer.equalsIgnoreCase("y")) {
                return true;
            }
            if (answer.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("Please enter y or n.");
        }
    }

    // Add an expense.
    public static void addExpense(
            Scanner input,
            ArrayList<Expense> expenses,
            ArrayList<Participant> participants) {
        if (participants.isEmpty()) {
            System.out.println("Add participant first.");
            return;
        }
        System.out.print("Enter the name of the expense: ");
        String name = input.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Expense name cannot be empty.");
            return;
        }
        System.out.println("1. Split among all participants");
        System.out.println("2. Split among selected participants");
        System.out.println("3. Split by items");
        System.out.print("Choose split type: ");
        String choice = input.nextLine().trim();
        if (choice.equals("3")) {
            addItemExpense(input, expenses, participants, name);
            return;
        }
        if (!choice.equals("1") && !choice.equals("2")) {
            System.out.println("Invalid choice.");
            return;
        }

        // Regular equal or selected-participant split.
        long food = askAmount(input, "Enter amount ($): ");
        long tax = askAmount(input, "Enter tax amount ($): ");
        long tip = askAmount(input, "Enter tip amount ($): ");
        long discount = askAmount(input, "Enter discount amount ($): ");
        long total = food + tax + tip - discount;
        if (discount > food + tax + tip || total > 100000000000L) {
            System.out.println("Invalid total amount.");
            return;
        }
        System.out.printf("Total amount: $%.2f%n", total / 100.0);
        System.out.print("Enter date: ");
        String date = input.nextLine().trim();
        System.out.print("Enter payer name: ");
        String payerName = input.nextLine().trim();
        int payerId = findParticipant(payerName, participants);
        if (payerId == -1) {
            System.out.println("Participant not found.");
            return;
        }
        ArrayList<Integer> sharedIds = new ArrayList<>();
        if (choice.equals("1")) {
            for (Participant person : participants) {
                sharedIds.add(person.getId());
            }
        } else {
            System.out.print(
                    "How many participants will share this expense? ");
            int count;
            try {
                count = Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
                return;
            }
            if (count < 1 || count > participants.size()) {
                System.out.println("Invalid participant count.");
                return;
            }
            while (sharedIds.size() < count) {
                System.out.print("Enter participant name: ");
                String sharedName = input.nextLine().trim();
                int id = findParticipant(sharedName, participants);
                if (id == -1) {
                    System.out.println("Participant not found.");
                } else if (sharedIds.contains(id)) {
                    System.out.println("Participant already selected.");
                } else {
                    sharedIds.add(id);
                }
            }
        }
        Expense expense = new Expense(
                name, total / 100.0, payerId, date, sharedIds);
        expenses.add(expense);
        System.out.println("Expense added successfully.");
    }

    // Add an expense divided by individual items.
    private static void addItemExpense(
            Scanner input,
            ArrayList<Expense> expenses,
            ArrayList<Participant> participants,
            String name) {
        ArrayList<Integer> dinerIds = new ArrayList<>();
        System.out.println("\n--- Participants ---");
        for (Participant person : participants) {
            dinerIds.add(person.getId());
            System.out.println(
                    person.getId() + ". " + person.getName());
        }
        int count = dinerIds.size();
        long[] foodTotals = new long[count];
        long[] taxableFoodTotals = new long[count];
        boolean[] joinedMeal = new boolean[count];
        long totalFood = 0;

        // Enter items one by one.
        while (true) {
            System.out.print("Enter item name: ");
            String itemName = input.nextLine().trim();
            if (itemName.isEmpty()) {
                System.out.println("Item name cannot be empty.");
                continue;
            }
            long itemCents = askAmount(
                    input, "Enter item price ($): ");
            System.out.print(
                    "Who shared this item? Enter IDs separated by commas: ");
            String idText = input.nextLine();
            String[] entries = idText.split(",");
            ArrayList<Integer> itemIds = new ArrayList<>();
            boolean valid = true;
            try {
                for (String entry : entries) {
                    int id = Integer.parseInt(entry.trim());
                    if (!dinerIds.contains(id)
                            || itemIds.contains(id)) {
                        valid = false;
                        break;
                    }
                    itemIds.add(id);
                }
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid || itemIds.isEmpty()) {
                System.out.println("Invalid participant IDs.");
                continue;
            }
            if (totalFood + itemCents > 100000000000L) {
                System.out.println("Food total is too large.");
                continue;
            }
            boolean taxable = askYesNo(
                    input, "Is this item taxable? (y/n): ");

            // Divide this item's cost among selected people.
            long[] shares = BillCalculator.splitEqually(
                    itemCents / 100.0, itemIds.size());
            for (int i = 0; i < itemIds.size(); i++) {
                int position = dinerIds.indexOf(itemIds.get(i));
                foodTotals[position] += shares[i];
                joinedMeal[position] = true;
                if (taxable) {
                    taxableFoodTotals[position] += shares[i];
                }
            }
            totalFood += itemCents;
            System.out.println("Added: " + itemName);
            if (!askYesNo(input, "Add another item? (y/n): ")) {
                break;
            }
        }
        if (totalFood == 0) {
            System.out.println("No food amount was added.");
            return;
        }

        // Calculate tax based on taxable food.
        long[] taxShares;
        while (true) {
            long receiptTax = askAmount(
                    input, "Enter receipt tax ($): ");
            try {
                taxShares = BillCalculator.splitProportionally(
                        receiptTax, taxableFoodTotals);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(
                        "No taxable items. Please enter 0 for tax.");
            }
        }

        // Count participants who joined the meal.
        int dinerCount = 0;
        for (boolean joined : joinedMeal) {
            if (joined) {
                dinerCount++;
            }
        }

        // Divide tip equally among diners.
        long receiptTip = askAmount(
                input, "Enter receipt tip ($): ");
        long[] tipShares = BillCalculator.splitEqually(
                receiptTip / 100.0, dinerCount);

        // Discount cannot exceed food subtotal.
        long receiptDiscount;
        while (true) {
            receiptDiscount = askAmount(
                    input, "Enter receipt discount ($): ");
            if (receiptDiscount <= totalFood) {
                break;
            }
            System.out.println(
                    "Discount cannot exceed the food subtotal.");
        }
        long[] discountShares = BillCalculator.splitProportionally(
                receiptDiscount, foodTotals);
        ArrayList<Integer> mealIds = new ArrayList<>();
        ArrayList<Long> owedAmounts = new ArrayList<>();
        long mealTotal = 0;
        int tipIndex = 0;
        System.out.println("\n--- Food Subtotals ---");
        for (int i = 0; i < count; i++) {
            long personalTip = 0;
            if (joinedMeal[i]) {
                personalTip = tipShares[tipIndex];
                tipIndex++;
            }
            long personalOwed = foodTotals[i]
                    + taxShares[i]
                    + personalTip
                    - discountShares[i];
            if (joinedMeal[i]) {
                mealIds.add(dinerIds.get(i));
                owedAmounts.add(personalOwed);
                mealTotal += personalOwed;
            }
            System.out.printf(
                    "%s: Food $%.2f | Tax $%.2f | Tip $%.2f | Discount $%.2f | Owed $%.2f%n",
                    participants.get(i).getName(),
                    foodTotals[i] / 100.0,
                    taxShares[i] / 100.0,
                    personalTip / 100.0,
                    discountShares[i] / 100.0,
                    personalOwed / 100.0);
        }
        if (mealTotal > 100000000000L) {
            System.out.println("Receipt total is too large.");
            return;
        }
        System.out.print("Enter date: ");
        String mealDate = input.nextLine().trim();
        int payerId = -1;
        while (payerId == -1) {
            System.out.print("Enter payer name: ");
            String payerName = input.nextLine().trim();
            payerId = findParticipant(payerName, participants);
            if (payerId == -1) {
                System.out.println(
                        "Participant not found. Please try again.");
            }
        }
        Expense expense = new Expense(
                name,
                mealTotal / 100.0,
                payerId,
                mealDate,
                mealIds,
                owedAmounts);
        expenses.add(expense);
        System.out.printf(
                "Expense added. Receipt total: $%.2f%n",
                mealTotal / 100.0);
    }

    // Display all recorded expenses.
    public static void viewExpense(
            ArrayList<Expense> expenses,
            ArrayList<Participant> participants) {
        if (expenses.isEmpty()) {
            System.out.println("There are no expenses to view.");
            return;
        }
        for (Expense expense : expenses) {
            System.out.println("\nName: " + expense.getDescription());
            System.out.printf(
                    "Amount: $%.2f%n", expense.getAmount());
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
            ArrayList<Long> owedAmounts = expense.getOwedAmounts();
            if (owedAmounts.isEmpty()) {
                long[] shares = BillCalculator.splitSharedExpense(
                        expense.getAmount(), sharedIds);
                for (long share : shares) {
                    owedAmounts.add(share);
                }
            }
            for (int i = 0; i < sharedIds.size(); i++) {
                String sharedName = "Unknown";
                for (Participant person : participants) {
                    if (person.getId() == sharedIds.get(i)) {
                        sharedName = person.getName();
                        break;
                    }
                }
                System.out.printf(
                        "%s owes: $%.2f%n",
                        sharedName,
                        owedAmounts.get(i) / 100.0);
            }
        }
    }
}
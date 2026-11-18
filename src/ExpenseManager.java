import java.math.BigDecimal;
import java.util.Arrays;
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
        System.out.println("1. Split among all participants");
        System.out.println("2. Split among selected participants");
        System.out.println("3. Split by items");
        System.out.print("Choose split type: ");
        String choice = input.nextLine().trim();

        if (choice.equals("3")) {
            ArrayList<Integer> dinerIds = new ArrayList<>();
            for (Participant person : participants) {
                dinerIds.add(person.getId());
                System.out.println(person.getId() + ". " + person.getName());
            }

            // Create one subtotal per person for the whole receipt.
            BigDecimal[] foodTotals = new BigDecimal[dinerIds.size()];
            Arrays.fill(foodTotals, BigDecimal.ZERO);
            boolean[] joinedMeal = new boolean[dinerIds.size()];
            BigDecimal[] taxableFoodTotals = new BigDecimal[dinerIds.size()];
            Arrays.fill(taxableFoodTotals, BigDecimal.ZERO);

            while (true) {
                System.out.print("Enter item name: ");
                String itemName = input.nextLine().trim();
                if (itemName.isEmpty()) {
                    System.out.println("Please enter an item name.");
                    continue;
                }

                System.out.print("Enter item price ($): ");
                String priceText = input.nextLine().trim();

                System.out.print("Who shared this item? Enter IDs separated by commas: ");
                String[] entries = input.nextLine().split(",", -1);
                ArrayList<Integer> itemIds = new ArrayList<>();

                try {
                    if (!priceText.matches("[0-9]+(\\.[0-9]{1,2})?")) {
                        System.out.println("Enter a price with at most two decimal places.");
                        continue;
                    }

                    double itemPrice = Double.parseDouble(priceText);
                    for (String entry : entries) {
                        itemIds.add(Integer.parseInt(entry.trim()));
                    }
                    BillCalculator.addItemToFoodTotals(itemPrice, itemIds, dinerIds, foodTotals);
                    String taxable;
                    do {
                        System.out.print("Is this item taxable? (y/n): ");
                        taxable = input.nextLine().trim();
                    } while (!taxable.equalsIgnoreCase("y")
                            && !taxable.equalsIgnoreCase("n"));

                    if (taxable.equalsIgnoreCase("y")) {
                        BillCalculator.addItemToFoodTotals(
                                itemPrice, itemIds, dinerIds, taxableFoodTotals);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Check the price and IDs. Please enter this item again.");
                    continue;
                }
                for (int id : itemIds) {
                    joinedMeal[dinerIds.indexOf(id)] = true;
                }

                System.out.println("Added: " + itemName);

                String answer;
                do {
                    System.out.print("Add another item? (y/n): ");
                    answer = input.nextLine().trim();
                } while (!answer.equalsIgnoreCase("y") && !answer.equalsIgnoreCase("n"));

                if (answer.equalsIgnoreCase("n")) {
                    break;
                }
            }
            BigDecimal[] taxShares;
            while (true) {
                System.out.print("Enter receipt tax ($): ");
                String taxText = input.nextLine().trim();

                if (!taxText.matches("[0-9]+(\\.[0-9]{1,2})?")) {
                    System.out.println("Enter an amount such as 0 or 1.11.");
                    continue;
                }

                BigDecimal receiptTax = new BigDecimal(taxText);
                try {
                    taxShares = BillCalculator.splitProportionally(
                            receiptTax, taxableFoodTotals);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("No taxable items. Enter 0 for tax.");
                }
            }
            int dinerCount = 0;
            for (boolean joined : joinedMeal) {
                if (joined) {
                    dinerCount++;
                }
            }

            BigDecimal receiptTip;
            while (true) {
                System.out.print("Enter receipt tip ($): ");
                String tipText = input.nextLine().trim();

                if (tipText.matches("[0-9]+(\\.[0-9]{1,2})?")) {
                    receiptTip = new BigDecimal(tipText);
                    break;
                }

                System.out.println("Enter an amount such as 0 or 3.01.");
            }

            BigDecimal tipPerDiner = receiptTip.divide(BigDecimal.valueOf(dinerCount), java.math.MathContext.DECIMAL128);
            BigDecimal totalFood = BigDecimal.ZERO;
            for (BigDecimal food : foodTotals) {
                totalFood = totalFood.add(food);
            }

            BigDecimal[] discountShares;
            while (true) {
                System.out.print("Enter receipt discount ($): ");
                String discountText = input.nextLine().trim();

                if (!discountText.matches("[0-9]+(\\.[0-9]{1,2})?")) {
                    System.out.println("Enter an amount such as 0 or 1.00.");
                    continue;
                }

                BigDecimal receiptDiscount = new BigDecimal(discountText);
                if (receiptDiscount.compareTo(totalFood) > 0) {
                    System.out.println("Discount cannot exceed the food subtotal.");
                    continue;
                }

                discountShares = BillCalculator.splitProportionally(
                        receiptDiscount, foodTotals);
                break;
            }
            ArrayList<Integer> mealIds = new ArrayList<>();
            ArrayList<BigDecimal> owedAmounts = new ArrayList<>();
            System.out.println("Food subtotals for " + name + ":");
            for (int i = 0; i < participants.size(); i++) {
                BigDecimal personalTip = BigDecimal.ZERO;
                if (joinedMeal[i]) {
                    personalTip = tipPerDiner;
                }

                BigDecimal personalOwed = foodTotals[i]
                        .add(taxShares[i])
                        .add(personalTip)
                        .subtract(discountShares[i]);
                if (joinedMeal[i]) {
                    mealIds.add(dinerIds.get(i));
                    owedAmounts.add(personalOwed);
                }
                System.out.printf(
                        "%s: Food $%.3f | Tax $%.3f | Tip $%.3f | Discount $%.3f | Owed $%.3f%n",
                        participants.get(i).getName(),
                        foodTotals[i], taxShares[i], personalTip,
                        discountShares[i], personalOwed);
            }

            System.out.print("Enter date: ");
            String mealDate = input.nextLine().trim();

            int mealPayerId = -1;
            while (mealPayerId == -1) {
                System.out.print("Enter payer name: ");
                String mealPayerName = input.nextLine().trim();

                for (Participant person : participants) {
                    if (person.getName().equalsIgnoreCase(mealPayerName)) {
                        mealPayerId = person.getId();
                        break;
                    }
                }

                if (mealPayerId == -1) {
                    System.out.println("Participant not found. Please try again.");
                }
            }

            BigDecimal mealTotal = BigDecimal.ZERO;
            for (BigDecimal owed : owedAmounts) {
                mealTotal = mealTotal.add(owed);
            }

            if (mealTotal.compareTo(new BigDecimal("1000000000")) > 0) {
                System.out.println("Total amount cannot exceed $1,000,000,000.");
                System.out.println("Expense was not added.");
                return;
            }

            Expense mealExpense = new Expense(
                    name, mealTotal.setScale(2, java.math.RoundingMode.HALF_UP)
                            .doubleValue(),
                    mealPayerId, mealDate, mealIds, owedAmounts);
            expenses.add(mealExpense);

            System.out.printf("Expense added. Receipt total: $%.2f%n", mealTotal);
            return;
        }
        if (!choice.equals("1") && !choice.equals("2")) {
            System.out.println("Invalid choice. Expense was not added.");
            return;
        }
        System.out.println("Enter amount: ");
        if (!input.hasNextDouble()) {
            input.next();
            input.nextLine();
            System.out.println("Please enter a valid number.");
            return;
        }
        double amount = input.nextDouble();
        System.out.print("Enter tax amount ($): ");
        if (!input.hasNextDouble()) {
            input.next();
            input.nextLine();
            System.out.println("Please enter a valid number.");
            return;
        }
        double tax = input.nextDouble();
        System.out.print("Enter tip amount ($): ");
        if (!input.hasNextDouble()) {
            input.next();
            input.nextLine();
            System.out.println("Please enter a valid number.");
            return;
        }
        double tip = input.nextDouble();
        System.out.print("Enter discount amount ($): ");
        if (!input.hasNextDouble()) {
            input.next();
            input.nextLine();
            System.out.println("Please enter a valid number.");
            return;
        }
        double discount = input.nextDouble();
        input.nextLine();
        if (!Double.isFinite(amount) || !Double.isFinite(tax)
            || !Double.isFinite(tip) || !Double.isFinite(discount)
            || amount < 0 || tax < 0 || tip < 0 || discount < 0) {
                System.out.println("Amounts cannot be negative or invalid.");
                return;
            }
            if (discount > amount + tax + tip) {
                System.out.println("Discount cannot exceed the total amount.");
                return;
            }
        double total = amount + tax + tip - discount;

        if (!Double.isFinite(total) || total > 1000000000) {
            System.out.println("Total amount cannot exceed $1,000,000,000.");
            return;
        }

        total = Math.round(total * 100) / 100.0;
        System.out.printf("Total amount: $%.2f%n", total);
        amount = total;
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

            ArrayList<BigDecimal> owedAmounts = expense.getOwedAmounts();

            if (owedAmounts.isEmpty()) {
                long[] shares = BillCalculator.splitSharedExpense(
                        expense.getAmount(), sharedIds);
                for (long share : shares) {
                    owedAmounts.add(BigDecimal.valueOf(share, 2));
                }
            }

            for (int i = 0; i < sharedIds.size(); i++) {
                String sharedName = "Unknown";

                for (Participant person : participants) {
                    if (person.getId() == sharedIds.get(i).intValue()) {
                        sharedName = person.getName();
                        break;
                    }
                }

                System.out.printf(
                        "%s owes: $%.3f%n",
                        sharedName,
                        owedAmounts.get(i));
            }
            System.out.println();
        }
    }
}
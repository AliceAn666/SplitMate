import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
public class DataManager {

    // Save participants and expenses to a text file.
    public static void saveData(
            ArrayList<Participant> participants,
            ArrayList<Expense> expenses) {
        try (PrintWriter writer = new PrintWriter(
                new File("splitmate.txt"), "UTF-8")) {

            // Version 3 stores owed amounts in cents.
            writer.println("SplitMate3");
            writer.println(participants.size());
            for (Participant person : participants) {
                writer.println(person.getId());
                writer.println(person.getName());
            }
            writer.println(expenses.size());
            for (Expense expense : expenses) {
                writer.println(expense.getDescription());
                writer.println(expense.getAmount());
                writer.println(expense.getDate());
                writer.println(expense.getPayerId());
                ArrayList<Integer> sharedIds =
                        expense.getSharedIds();
                writer.println(sharedIds.size());
                for (int id : sharedIds) {
                    writer.println(id);
                }
                ArrayList<Long> owedAmounts =
                        expense.getOwedAmounts();
                writer.println(owedAmounts.size());
                for (long owed : owedAmounts) {
                    writer.println(owed);
                }
            }
            if (writer.checkError()) {
                System.out.println("Could not save all data.");
                return;
            }
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Could not save the file.");
        }
    }

    // Load participants and expenses from the text file.
    public static void loadData(
            ArrayList<Participant> participants,
            ArrayList<Expense> expenses) {
        File file = new File("splitmate.txt");
        if (!file.exists()) {
            System.out.println("No saved file found.");
            return;
        }

        // Temporary lists protect current data if loading fails.
        ArrayList<Participant> loadedParticipants =
                new ArrayList<>();
        ArrayList<Expense> loadedExpenses =
                new ArrayList<>();
        try (Scanner reader = new Scanner(file, "UTF-8")) {
            String version = reader.nextLine();
            if (!version.equals("SplitMate1")
                    && !version.equals("SplitMate2")
                    && !version.equals("SplitMate3")) {
                throw new IllegalArgumentException();
            }

            // Load participants.
            int participantCount =
                    Integer.parseInt(reader.nextLine());
            if (participantCount < 0) {
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < participantCount; i++) {
                int id = Integer.parseInt(reader.nextLine());
                String name = reader.nextLine().trim();
                if (id <= 0 || name.isEmpty()) {
                    throw new IllegalArgumentException();
                }
                for (Participant person : loadedParticipants) {
                    if (person.getId() == id
                            || person.getName().equalsIgnoreCase(name)) {
                        throw new IllegalArgumentException();
                    }
                }
                loadedParticipants.add(
                        new Participant(id, name));
            }

            // Load expenses.
            int expenseCount =
                    Integer.parseInt(reader.nextLine());
            if (expenseCount < 0) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < expenseCount; i++) {
                String description = reader.nextLine();
                double amount =
                        Double.parseDouble(reader.nextLine());
                String date = reader.nextLine();
                int payerId =
                        Integer.parseInt(reader.nextLine());
                if (!Double.isFinite(amount)
                        || amount < 0
                        || amount > 1000000000
                        || !hasParticipant(
                                payerId, loadedParticipants)) {
                    throw new IllegalArgumentException();
                }
                long amountCents =
                        BillCalculator.toCents(amount);
                int sharedCount =
                        Integer.parseInt(reader.nextLine());
                if (sharedCount <= 0
                        || sharedCount > loadedParticipants.size()) {
                    throw new IllegalArgumentException();
                }
                ArrayList<Integer> sharedIds =
                        new ArrayList<>();
                for (int j = 0; j < sharedCount; j++) {
                    int id =
                            Integer.parseInt(reader.nextLine());
                    if (!hasParticipant(
                            id, loadedParticipants)
                            || sharedIds.contains(id)) {
                        throw new IllegalArgumentException();
                    }
                    sharedIds.add(id);
                }
                ArrayList<Long> owedAmounts =
                        new ArrayList<>();

                // Version 1 does not store individual shares.
                if (!version.equals("SplitMate1")) {
                    int owedCount =
                            Integer.parseInt(reader.nextLine());
                    if (owedCount != 0
                            && owedCount != sharedIds.size()) {
                        throw new IllegalArgumentException();
                    }
                    long totalOwed = 0;
                    if (version.equals("SplitMate3")) {

                        // Version 3 stores whole cents.
                        for (int j = 0; j < owedCount; j++) {
                            long owed =
                                    Long.parseLong(reader.nextLine());
                            if (owed < 0 || owed > amountCents) {
                                throw new IllegalArgumentException();
                            }
                            owedAmounts.add(owed);
                            totalOwed += owed;
                        }
                    } else {

                        // Version 2 stores dollar amounts.
                        // Convert old amounts to cents.
                        double[] oldAmounts = new double[owedCount];
                        double oldTotal = 0;
                        for (int j = 0; j < owedCount; j++) {
                            double owed =
                                    Double.parseDouble(reader.nextLine());
                            if (!Double.isFinite(owed)
                                    || owed < 0
                                    || owed > 1000000000) {
                                throw new IllegalArgumentException();
                            }
                            oldAmounts[j] = owed;
                            oldTotal += owed;
                        }
                        if (owedCount > 0
                                && Math.abs(oldTotal - amount)
                                        > 0.000001) {
                            throw new IllegalArgumentException();
                        }
                        for (int j = 0; j < owedCount; j++) {
                            long owedCents = Math.round(
                                    oldAmounts[j] * 100);
                            owedAmounts.add(owedCents);
                            totalOwed += owedCents;
                        }

                        // Adjust rounding differences in old files.
                        while (totalOwed != amountCents
                                && owedCount > 0) {
                            int best = -1;
                            double bestDifference = 0;
                            for (int j = 0; j < owedCount; j++) {
                                double difference =
                                        oldAmounts[j] * 100
                                        - owedAmounts.get(j);
                                if (totalOwed < amountCents) {
                                    if (best == -1
                                            || difference > bestDifference) {
                                        best = j;
                                        bestDifference = difference;
                                    }
                                } else {
                                    if (owedAmounts.get(j) > 0
                                            && (best == -1
                                            || difference < bestDifference)) {
                                        best = j;
                                        bestDifference = difference;
                                    }
                                }
                            }
                            if (best == -1) {
                                throw new IllegalArgumentException();
                            }
                            if (totalOwed < amountCents) {
                                owedAmounts.set(
                                        best, owedAmounts.get(best) + 1);
                                totalOwed++;
                            } else {
                                owedAmounts.set(
                                        best, owedAmounts.get(best) - 1);
                                totalOwed--;
                            }
                        }
                    }

                    // Check that shares match the receipt total.
                    if (!owedAmounts.isEmpty()
                            && totalOwed != amountCents) {
                        throw new IllegalArgumentException();
                    }
                }
                Expense expense;
                if (owedAmounts.isEmpty()) {
                    expense = new Expense(
                            description,
                            amount,
                            payerId,
                            date,
                            sharedIds);
                } else {
                    expense = new Expense(
                            description,
                            amount,
                            payerId,
                            date,
                            sharedIds,
                            owedAmounts);
                }
                loadedExpenses.add(expense);
            }

            // Reject unexpected extra data.
            if (reader.hasNextLine()) {
                throw new IllegalArgumentException();
            }
            if (reader.ioException() != null) {
                throw new IOException();
            }

            // Replace current data only after loading succeeds.
            participants.clear();
            participants.addAll(loadedParticipants);
            expenses.clear();
            expenses.addAll(loadedExpenses);
            System.out.println("Data loaded successfully.");
        } catch (IOException e) {
            System.out.println("Could not read the file.");
        } catch (IllegalArgumentException
                 | java.util.NoSuchElementException e) {
            System.out.println(
                    "Invalid file data. Current data was kept.");
        }
    }

    // Check whether a participant ID exists.
    private static boolean hasParticipant(
            int id,
            ArrayList<Participant> participants) {
        for (Participant person : participants) {
            if (person.getId() == id) {
                return true;
            }
        }
        return false;
    }
}
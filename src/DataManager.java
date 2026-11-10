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

            writer.println("SplitMate1");

            // Save participants.
            writer.println(participants.size());

            for (Participant person : participants) {
                writer.println(person.getId());
                writer.println(person.getName());
            }

            // Save expenses.
            writer.println(expenses.size());

            for (Expense expense : expenses) {
                writer.println(expense.getDescription());
                writer.println(expense.getAmount());
                writer.println(expense.getDate());
                writer.println(expense.getPayerId());

                ArrayList<Integer> sharedIds = expense.getSharedIds();
                writer.println(sharedIds.size());

                for (int id : sharedIds) {
                    writer.println(id);
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

    // Load data only after the whole file has been checked.
    public static void loadData(
            ArrayList<Participant> participants,
            ArrayList<Expense> expenses) {

        File file = new File("splitmate.txt");

        if (!file.exists()) {
            System.out.println("No saved file found.");
            return;
        }

        ArrayList<Participant> loadedParticipants = new ArrayList<>();
        ArrayList<Expense> loadedExpenses = new ArrayList<>();

        try (Scanner reader = new Scanner(file, "UTF-8")) {

            if (!reader.nextLine().equals("SplitMate1")) {
                throw new IllegalArgumentException();
            }

            int participantCount = Integer.parseInt(reader.nextLine());

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

                loadedParticipants.add(new Participant(id, name));
            }

            int expenseCount = Integer.parseInt(reader.nextLine());

            if (expenseCount < 0) {
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < expenseCount; i++) {
                String description = reader.nextLine();
                double amount = Double.parseDouble(reader.nextLine());
                String date = reader.nextLine();
                int payerId = Integer.parseInt(reader.nextLine());

                if (!Double.isFinite(amount)
                        || amount < 0 || amount > 1000000000
                        || !hasParticipant(payerId, loadedParticipants)) {
                    throw new IllegalArgumentException();
                }

                int sharedCount = Integer.parseInt(reader.nextLine());

                if (sharedCount <= 0
                        || sharedCount > loadedParticipants.size()) {
                    throw new IllegalArgumentException();
                }

                ArrayList<Integer> sharedIds = new ArrayList<>();

                for (int j = 0; j < sharedCount; j++) {
                    int id = Integer.parseInt(reader.nextLine());

                    if (!hasParticipant(id, loadedParticipants)
                            || sharedIds.contains(id)) {
                        throw new IllegalArgumentException();
                    }

                    sharedIds.add(id);
                }

                Expense expense = new Expense(
                        description, amount, payerId, date, sharedIds);

                loadedExpenses.add(expense);
            }

            if (reader.hasNextLine()) {
                throw new IllegalArgumentException();
            }

            if (reader.ioException() != null) {
                throw new IOException();
            }

            // Replace current data after loading succeeds.
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
            int id, ArrayList<Participant> participants) {

        for (Participant person : participants) {
            if (person.getId() == id) {
                return true;
            }
        }

        return false;
    }
}
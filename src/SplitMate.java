import java.util.ArrayList;
import java.util.Scanner;
public class SplitMate {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        ArrayList<Participant> participants = new ArrayList<>();
        String choice;
        do {
            displayMenu();
            choice = input.nextLine().trim();
            switch (choice) {
                case "1":
                    addParticipant(input, participants);
                    break;
                case "2":
                    viewParticipants(participants);
                    break;
                case "3":
                    System.out.println("This feature is coming soon.");
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            } 
        }while (!choice.equals("0")); 
        input.close();
    }
    public static void displayMenu() {
        System.out.println("\n===== SplitMate =====");
        System.out.println("1. Add participant");
        System.out.println("2. View participants");
        System.out.println("3. Add expense");
        System.out.println("0. Exit");
        System.out.print("Enter your choise: ");
    }
    public static void addParticipant (Scanner input, ArrayList<Participant> participants) {
        System.out.print("Enter participant name: ");
        String name = input.nextLine().trim();
        if(name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        for (Participant person: participants){
            if (person.getName().equalsIgnoreCase(name)){
                System.out.println("This name already exists.");
                return; 
            }
        }
        int id =participants.size() + 1;
        Participant person = new Participant(id, name);
        participants.add(person);
        System.out.println("Participant added: " + id + " - " + name);
    }
    public static void viewParticipants(ArrayList<Participant> participants) {
        if (participants.isEmpty()) {
            System.out.println("No participants yet.");
            return;
        }
        System.out.println("\n--- Participants ---");
        for (Participant person : participants) {
            System.out.println(person.getId() + " - " + person.getName());
        }
    }
}
import java.util.Scanner;

public class Yuri {

    private static final String BOT_NAME = "Yuri";
    private static final String HLINE = "____________________________________________________________";

    private static void printGreeting() {
        System.out.println(HLINE);
        System.out.println(" Hello! I'm " + BOT_NAME);
        System.out.println(" What can I do for you?");
        System.out.println(" (Friendly tip: type anything and I'll echo it. Type 'bye' to exit!)");
        System.out.println(HLINE);
    }

    private static void printFarewell() {
        System.out.println(HLINE);
        System.out.println(" Bye. Hope to see you again soon!");
        System.out.println(" That was fun—high five for productivity!");
        System.out.println(HLINE);
    }

    public static void main(String[] args) {
        printGreeting();

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                if (!sc.hasNextLine()) { // EOF (e.g., Ctrl+D)
                    printFarewell();
                    break;
                }
                String line = sc.nextLine();
                if (line.trim().equalsIgnoreCase("bye")) {
                    printFarewell();
                    break;
                }

                System.out.println(line);
            }
        }
    }
}

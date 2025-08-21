import java.util.Scanner;

public class Yuri {

    private static final String BOT_NAME = "Yuri";

    private static final String HLINE = "____________________________________________________________";

    private static final int MAX_TASKS = 100;
    private static final String[] tasks = new String[MAX_TASKS];
    private static int taskCount = 0;


    private static void printGreeting() {
        System.out.println(HLINE);
        System.out.println(" Hello! I'm " + BOT_NAME);
        System.out.println(" What can I do for you?");

        System.out.println(HLINE);
    }

    private static void printFarewell() {
        System.out.println(HLINE);
        System.out.println(" Bye. Hope to see you again soon!");
        System.out.println(" That was fun—high five for productivity!");
        System.out.println(HLINE);
    }

    private static void printBlock(String... lines) {
        System.out.println(HLINE);
        for (String s : lines) {
            System.out.println(s);
        }
        System.out.println(HLINE);
    }

    private static void addTask(String task) {
        if (taskCount >= MAX_TASKS) {
            printBlock(" Sorry, I can't remember more than " + MAX_TASKS + " tasks.");
            return;
        }
        tasks[taskCount++] = task;
        printBlock(" added: " + task);
    }

    private static void printList() {
        System.out.println(HLINE);
        if (taskCount > 0) {
            // (Optional line in samples; including it won’t hurt)
            // System.out.println(" Here are the tasks in your list:");
            for (int i = 0; i < taskCount; i++) {
                System.out.println(" " + (i + 1) + ". " + tasks[i]);
            }
        }
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
                if (line.equalsIgnoreCase("bye")) {
                    printFarewell();
                    break;
                } else if (line.equalsIgnoreCase("list")) {
                    printList();
                } else if (!line.isEmpty()) {
                    addTask(line);
                }

            }
        }
    }

    static class Task {
        private final String description;
        private boolean isDone;

        Task(String description) {
            this.description = description;
            this.isDone = false;
        }

        void mark() { this.isDone = true; }
        void unmark() { this.isDone = false; }

        String getStatusIcon() { return isDone ? "X" : " "; }

        @Override
        public String toString() {
            return "[" + getStatusIcon() + "] " + description;
        }
    }

}

import java.util.Scanner;

public class Yuri {

    private static final String BOT_NAME = "Yuri";

    private static final String HLINE = "____________________________________________________________";

    private static final int MAX_TASKS = 100;
    //private static final String[] tasks = new String[MAX_TASKS];
    private static final Task[] tasks = new Task[MAX_TASKS];
    private static int taskCount = 0;


    private static void printGreeting() {
        System.out.println(HLINE);
        System.out.println(" Hello! I'm " + BOT_NAME);
        System.out.println(" What can I do for you?");
        System.out.println(" (Tip: type anything to add; 'list' to see; 'mark n'/'unmark n'; 'bye' to exit.)");
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



    private static void addTask(String description) {
        if (taskCount >= MAX_TASKS) {
            printBlock(" Sorry, I can't remember more than " + MAX_TASKS + " tasks.");
            return;
        }
        tasks[taskCount++] = new Task(description);
        printBlock(" added: " + description);
    }

    private static void printList() {
        System.out.println(HLINE);
        System.out.println(" Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            // Format matches sample exactly: " 1.[X] read book"
            System.out.println(" " + (i + 1) + "." + tasks[i]);
        }
        System.out.println(HLINE);
    }

    private static void handleMark(String line) {
        Integer idx = parseIndex(line, "mark");
        if (idx == null) {
            // Keep outputs minimal at Level-3; strict error handling comes later.
            return;
        }
        int zeroBased = idx - 1;
        if (!isValidIndex(zeroBased)) {
            return;
        }
        tasks[zeroBased].mark();
        printBlock(
                " Nice! I've marked this task as done:",
                "   " + tasks[zeroBased]
        );
    }

    private static void handleUnmark(String line) {
        Integer idx = parseIndex(line, "unmark");
        if (idx == null) {
            return;
        }
        int zeroBased = idx - 1;
        if (!isValidIndex(zeroBased)) {
            return;
        }
        tasks[zeroBased].unmark();
        printBlock(
                " OK, I've marked this task as not done yet:",
                "   " + tasks[zeroBased]
        );
    }

    private static Integer parseIndex(String line, String cmd) {
        // Accept forms like "mark 2" or "unmark 10"
        String[] parts = line.split("\\s+");
        if (parts.length < 2) return null;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isValidIndex(int i) {
        return i >= 0 && i < taskCount;
    }


    public static void main(String[] args) {
        printGreeting();

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                if (!sc.hasNextLine()) { // EOF
                    printFarewell();
                    break;
                }
                String line = sc.nextLine().trim();

                if (line.equalsIgnoreCase("bye")) {
                    printFarewell();
                    break;
                } else if (line.equalsIgnoreCase("list")) {
                    printList();
                } else if (line.toLowerCase().startsWith("mark ")) {
                    handleMark(line);
                } else if (line.toLowerCase().startsWith("unmark ")) {
                    handleUnmark(line);
                } else if (!line.isEmpty()) {
                    addTask(line);
                }
                // empty line => ignore
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

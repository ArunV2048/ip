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



    private static void addTask(Task task) {
        if (taskCount >= MAX_TASKS) {
            printBlock(" Sorry, I can't remember more than " + MAX_TASKS + " tasks.");
            return;
        }
        tasks[taskCount++] = task;
        System.out.println(HLINE);
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + taskCount + " tasks in the list.");
        System.out.println(HLINE);
    }

    // (Kept for backward compatibility if you type free text; now treated as Todo)
    private static void addTask(String description) {
        addTask(new Todo(description));
    }

    private static void printList() {
        System.out.println(HLINE);
        System.out.println(" Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.println(" " + (i + 1) + "." + tasks[i]);
        }
        System.out.println(HLINE);
    }


    private static void handleMark(String line) {
        Integer idx = parseIndex(line, "mark");
        if (idx == null) {
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

    private static void handleTodo(String line) {
        // "todo <description>"
        String desc = line.length() > 4 ? line.substring(4).trim() : "";
        addTask(new Todo(desc));
    }

    private static void handleDeadline(String line) {
        // "deadline <description> /by <when>"
        String payload = line.length() > 8 ? line.substring(8).trim() : "";
        String[] parts = splitOnce(payload, "/by");
        String desc = parts[0].trim();
        String by = parts[1].trim();
        addTask(new Deadline(desc, by));
    }

    private static void handleEvent(String line) {
        // "event <description> /from <start> /to <end>"
        String payload = line.length() > 5 ? line.substring(5).trim() : "";
        String[] pFrom = splitOnce(payload, "/from");
        String desc = pFrom[0].trim();
        String rest = pFrom[1].trim();
        String[] pTo = splitOnce(rest, "/to");
        String from = pTo[0].trim();
        String to = pTo[1].trim();
        addTask(new Event(desc, from, to));
    }

    private static boolean startsWithWord(String line, String word) {
        String lw = line.toLowerCase();
        String ww = word.toLowerCase();
        return lw.equals(ww) || lw.startsWith(ww + " ");
    }

    private static String[] splitOnce(String s, String token) {
        int pos = indexOfToken(s, token);
        if (pos < 0) {
            return new String[] { s, "" };
        }
        String left = s.substring(0, pos);
        String right = s.substring(pos + token.length()).trim();
        return new String[] { left, right };
    }

    private static int indexOfToken(String s, String token) {
        String low = s.toLowerCase();
        String t = token.toLowerCase();
        int i = low.indexOf(t);
        if (i >= 0) return i;
        i = low.indexOf(" " + t);
        if (i >= 0) return i + 1;
        return -1;
    }


    private static Integer parseIndex(String line, String cmd) {
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
                if (!sc.hasNextLine()) {
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
                } else if (startsWithWord(line, "todo")) {
                    handleTodo(line);
                } else if (startsWithWord(line, "deadline")) {
                    handleDeadline(line);
                } else if (startsWithWord(line, "event")) {
                    handleEvent(line);
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

    static class Todo extends Task {
        Todo(String description) {
            super(description);
        }
        @Override
        public String toString() {
            return "[T]" + super.toString();
        }
    }

    static class Deadline extends Task {
        private final String by;
        Deadline(String description, String by) {
            super(description);
            this.by = by;
        }
        @Override
        public String toString() {
            return "[D]" + super.toString() + " (by: " + by + ")";
        }
    }

    static class Event extends Task {
        private final String from;
        private final String to;
        Event(String description, String from, String to) {
            super(description);
            this.from = from;
            this.to = to;
        }
        @Override
        public String toString() {
            return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
        }
    }
}

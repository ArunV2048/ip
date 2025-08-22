import java.util.Scanner;

public class Yuri {

    private static final String BOT_NAME = "Yuri";

    private static final String HLINE = "____________________________________________________________";

    private static final int MAX_TASKS = 100;
    //private static final String[] tasks = new String[MAX_TASKS];
    private static final Task[] tasks = new Task[MAX_TASKS];
    private static int taskCount = 0;

    //UI HELPERS


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

    private static void printError(String message) {
        printBlock(" OOPS!!! " + message);
    }

    // ADD/LIST/MARK/UNMARK


    private static void addTask(Task task) throws YuriException {
        if (task == null) {
            throw new YuriException("That task looks empty. Try again?");
        }
        if (task.description == null || task.description.trim().isEmpty()) {
            throw new YuriException("Task description cannot be empty.");
        }
        if (taskCount >= MAX_TASKS) {
            throw new YuriException("I can remember only " + MAX_TASKS + " tasks. Time to complete a few!");
        }
        tasks[taskCount++] = task;
        System.out.println(HLINE);
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + taskCount + " tasks in the list.");
        System.out.println(HLINE);
    }

    // (Kept for backward compatibility if you type free text; now treated as Todo)
//    private static void addTask(String description) {
//        addTask(new Todo(description));
//    }

    private static void printList() {
        System.out.println(HLINE);
        System.out.println(" Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.println(" " + (i + 1) + "." + tasks[i]);
        }
        System.out.println(HLINE);
    }


    private static void handleMark(String line) throws YuriException {
        int idx = parseIndexOrThrow(line, "mark");
        int zeroBased = idx - 1;
        requireValidIndex(zeroBased);
        tasks[zeroBased].mark();
        printBlock(
                " Nice! I've marked this task as done:",
                "   " + tasks[zeroBased]
        );
    }


    private static void handleUnmark(String line) throws YuriException {
        int idx = parseIndexOrThrow(line, "unmark");
        int zeroBased = idx - 1;
        requireValidIndex(zeroBased);
        tasks[zeroBased].unmark();
        printBlock(
                " OK, I've marked this task as not done yet:",
                "   " + tasks[zeroBased]
        );
    }

    //LEVEL 4 COMMANDS

    private static void handleTodo(String line) throws YuriException {
        // "todo <description>"
        String desc = sliceAfter(line, "todo");
        if (desc.isBlank()) {
            throw new YuriException("The description of a todo cannot be empty.");
        }
        addTask(new Todo(desc));
    }

    private static void handleDeadline(String line) throws YuriException {
        // "deadline <description> /by <when>"
        String payload = sliceAfter(line, "deadline");
        String[] parts = splitOnceOrThrow(payload, "/by",
                "Deadline needs '/by <when>'. Example: deadline return book /by Sunday");
        String desc = parts[0].trim();
        String by = parts[1].trim();
        if (desc.isEmpty()) throw new YuriException("Deadline description cannot be empty.");
        if (by.isEmpty()) throw new YuriException("Please specify when the deadline is due after '/by'.");
        addTask(new Deadline(desc, by));
    }


    private static void handleEvent(String line) throws YuriException {
        // "event <description> /from <start> /to <end>"
        String payload = sliceAfter(line, "event");
        String[] pFrom = splitOnceOrThrow(payload, "/from",
                "Event needs '/from <start>'. Example: event meeting /from Mon 2pm /to 3pm");
        String desc = pFrom[0].trim();
        String rest = pFrom[1].trim();
        String[] pTo = splitOnceOrThrow(rest, "/to",
                "Event needs '/to <end>'. Example: event meeting /from Mon 2pm /to 3pm");
        String from = pTo[0].trim();
        String to = pTo[1].trim();
        if (desc.isEmpty()) throw new YuriException("Event description cannot be empty.");
        if (from.isEmpty()) throw new YuriException("Please specify the event start after '/from'.");
        if (to.isEmpty()) throw new YuriException("Please specify the event end after '/to'.");
        addTask(new Event(desc, from, to));
    }

    //HELPERS


    private static boolean startsWithWord(String line, String word) {
        String lw = line.toLowerCase();
        String ww = word.toLowerCase();
        return lw.equals(ww) || lw.startsWith(ww + " ");
    }

    private static String sliceAfter(String line, String headWord) {
        // returns everything AFTER the command word
        String trimmed = line.trim();
        if (trimmed.length() <= headWord.length()) return "";
        return trimmed.substring(headWord.length()).trim();
    }

    private static String[] splitOnceOrThrow(String s, String token, String errMsg) throws YuriException {
        int pos = indexOfToken(s, token);
        if (pos < 0) throw new YuriException(errMsg);
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

    private static int parseIndexOrThrow(String line, String cmd) throws YuriException {
        String[] parts = line.split("\\s+");
        if (parts.length < 2) throw new YuriException("Please provide an index: '" + cmd + " <number>'.");
        try {
            int idx = Integer.parseInt(parts[1]);
            if (idx <= 0) throw new NumberFormatException();
            return idx;
        } catch (NumberFormatException e) {
            throw new YuriException("Index must be a positive number. Example: '" + cmd + " 2'.");
        }
    }

    private static void requireValidIndex(int i) throws YuriException {
        if (i < 0 || i >= taskCount) {
            throw new YuriException("That task number doesn't exist yet. Try 'list' to see valid numbers.");
        }
    }

    //MAIN

    public static void main(String[] args) {
        printGreeting();

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                if (!sc.hasNextLine()) {
                    printFarewell();
                    break;
                }
                String line = sc.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    if (line.equalsIgnoreCase("bye")) {
                        printFarewell();
                        break;
                    } else if (line.equalsIgnoreCase("list")) {
                        printList();
                    } else if (startsWithWord(line, "mark")) {
                        handleMark(line);
                    } else if (startsWithWord(line, "unmark")) {
                        handleUnmark(line);
                    } else if (startsWithWord(line, "todo")) {
                        handleTodo(line);
                    } else if (startsWithWord(line, "deadline")) {
                        handleDeadline(line);
                    } else if (startsWithWord(line, "event")) {
                        handleEvent(line);
                    } else {
                        // Unknown command
                        throw new YuriException("I don’t recognize that command. Try: todo, deadline, event, list, mark, unmark, bye.");
                    }
                } catch (YuriException e) {
                    printError(e.getMessage());
                }
            }
        }
    }

    //TASK + SUBCLASSES (INNER)

    static class Task {
        protected final String description;
        protected boolean isDone;

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
        Todo(String description) { super(description); }
        @Override
        public String toString() { return "[T]" + super.toString(); }
    }

    static class Deadline extends Task {
        private final String by;
        Deadline(String description, String by) {
            super(description);
            this.by = by;
        }
        @Override
        public String toString() { return "[D]" + super.toString() + " (by: " + by + ")"; }
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

    //CUSTOM EXCEPTION

    static class YuriException extends Exception {
        YuriException(String message) {
            super(message);
        }
    }

}

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Yuri {

    private static final String BOT_NAME = "Yuri";

    private static final String HLINE = "____________________________________________________________";
    private static final List<Task> tasks = new ArrayList<>();


    private static final int MAX_TASKS = 100;
    //private static final String[] tasks = new String[MAX_TASKS];
    //private static final Task[] tasks = new Task[MAX_TASKS];
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
        for (String s : lines) System.out.println(s);
        System.out.println(HLINE);
    }

    private static void printError(String message) {
        printBlock(" OOPS!!! " + message);
    }

    // ADD/LIST/MARK/UNMARK


    private static void addTask(Task task) throws YuriException {
        if (task == null || task.description == null || task.description.trim().isEmpty()) {
            throw new YuriException("Task description cannot be empty.");
        }
        tasks.add(task);
        System.out.println(HLINE);
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + tasks.size() + " tasks in the list.");

        try {
            new Yuri().new Storage("data/duke.txt").save(new ArrayList<>(tasks));
        } catch (IOException e) {
            printError("Failed to save: " + e.getMessage());
        }

        System.out.println(HLINE);
    }

    // (Kept for backward compatibility if you type free text; now treated as Todo)
//    private static void addTask(String description) {
//        addTask(new Todo(description));
//    }

    private static void printList() {
        System.out.println(HLINE);
        System.out.println(" Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + "." + tasks.get(i));
        }
        System.out.println(HLINE);
    }


    private static void handleMark(String line) throws YuriException {
        int idx = parseIndexOrThrow(line, "mark");
        int i = idx - 1;
        requireValidIndex(i);
        tasks.get(i).mark();
        printBlock(
                " Nice! I've marked this task as done:",
                "   " + tasks.get(i)
        );
    }


    private static void handleUnmark(String line) throws YuriException {
        int idx = parseIndexOrThrow(line, "unmark");
        int i = idx - 1;
        requireValidIndex(i);
        tasks.get(i).unmark();
        printBlock(
                " OK, I've marked this task as not done yet:",
                "   " + tasks.get(i)
        );
    }

    private static void handleDelete(String line) throws YuriException {
        int idx = parseIndexOrThrow(line, "delete");
        int i = idx - 1;
        requireValidIndex(i);
        Task removed = tasks.remove(i);
        System.out.println(HLINE);
        System.out.println(" Noted. I've removed this task:");
        System.out.println("   " + removed);
        System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
        System.out.println(HLINE);
    }


    //LEVEL 4 COMMANDS

    private static void handleTodo(String line) throws YuriException {
        String desc = sliceAfter(line, "todo");
        if (desc.isBlank()) throw new YuriException("The description of a todo cannot be empty.");
        addTask(new Todo(desc));
    }

    private static void handleDeadline(String line) throws YuriException {
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
        if (parts.length != 2) {
            throw new YuriException("Use: '" + cmd + " <number>' with exactly one number.");
        }
        try {
            int idx = Integer.parseInt(parts[1]);
            if (idx <= 0) throw new NumberFormatException();
            return idx;
        } catch (NumberFormatException e) {
            throw new YuriException("Index must be a positive number. Example: '" + cmd + " 2'.");
        }
    }

    private static void requireValidIndex(int i) throws YuriException {
        if (i < 0 || i >= tasks.size()) {
            throw new YuriException("That task number doesn't exist yet. Try 'list' to see valid numbers.");
        }
    }

    //MAIN

    public static void main(String[] args) {

        try {
            ArrayList<Task> loaded = new Yuri().new Storage("data/duke.txt").load();
            tasks.addAll(loaded);
        } catch (IOException e) {
            printError("Error loading save file: " + e.getMessage());
        }

        printGreeting();

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                if (!sc.hasNextLine()) {
                    printFarewell();
                    break;
                }
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                try {
                    if (line.toLowerCase().startsWith("list")) {
                        if (line.strip().contains(" ")) {
                            throw new YuriException("Just type 'list' with no extra words.");
                        }
                        printList();
                    } else if (line.toLowerCase().startsWith("bye")) {
                        if (line.strip().contains(" ")) {
                            throw new YuriException("Just type 'bye' with no extra words.");
                        }
                        printFarewell();
                        break;
                    } else if (startsWithWord(line, "mark")) {
                        handleMark(line);
                    } else if (startsWithWord(line, "unmark")) {
                        handleUnmark(line);
                    } else if (startsWithWord(line, "delete")) {
                        handleDelete(line);
                    } else if (startsWithWord(line, "todo")) {
                        handleTodo(line);
                    } else if (startsWithWord(line, "deadline")) {
                        handleDeadline(line);
                    } else if (startsWithWord(line, "event")) {
                        handleEvent(line);
                    } else {
                        throw new YuriException("I don’t recognize that command. Try: todo, deadline, event, list, mark, unmark, delete, bye.");
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

        public String toSaveFormat() {
            // Example for a Todo task
            // You can customize your encoding (e.g. T | 1 | read book)
            return "T | " + (isDone ? "1" : "0") + " | " + description;
        }
    }

    static class Todo extends Task {
        Todo(String description) { super(description); }
        @Override public String toString() { return "[T]" + super.toString(); }
        @Override public String toSaveFormat() {
            return "T | " + (isDone ? "1" : "0") + " | " + description;
        }
    }

    static class Deadline extends Task {
        private final String by;
        Deadline(String description, String by) { super(description); this.by = by; }
        @Override public String toString() { return "[D]" + super.toString() + " (by: " + by + ")"; }
        @Override public String toSaveFormat() {
            return "D | " + (isDone ? "1" : "0") + " | " + description + " | " + by;
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
        @Override public String toString() {
            return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
        }
        @Override public String toSaveFormat() {
            return "E | " + (isDone ? "1" : "0") + " | " + description + " | " + from + " | " + to;
        }
    }


    //CUSTOM EXCEPTION

    static class YuriException extends Exception {
        YuriException(String message) {
            super(message);
        }
    }

    public class Storage {
        private final String filePath;

        public Storage(String filePath) {
            this.filePath = filePath;
        }

        public ArrayList<Task> load() throws IOException {
            ArrayList<Task> tasks = new ArrayList<>();
            File file = new File(filePath);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                return tasks; // empty list
            }

            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Task task = parseTask(line); // <-- you’ll implement this
                tasks.add(task);
            }
            sc.close();
            return tasks;
        }

        public void save(ArrayList<Task> tasks) throws IOException {
            FileWriter fw = new FileWriter(filePath);
            for (Task task : tasks) {
                fw.write(task.toSaveFormat() + System.lineSeparator());
            }
            fw.close();
        }

        private Task parseTask(String line) {
            // Expected formats:
            // T | 0/1 | description
            // D | 0/1 | description | by
            // E | 0/1 | description | from | to
            String[] parts = line.split("\\s*\\|\\s*");
            if (parts.length < 3) return null; // or throw

            String type = parts[0];
            boolean done = "1".equals(parts[1]);
            Task t;

            switch (type) {
                case "T":
                    t = new Todo(parts[2]);
                    break;
                case "D":
                    if (parts.length < 4) return null;
                    t = new Deadline(parts[2], parts[3]);
                    break;
                case "E":
                    if (parts.length < 5) return null;
                    t = new Event(parts[2], parts[3], parts[4]);
                    break;
                default:
                    return null;
            }
            if (done) t.mark();
            return t;
        }

    }
}



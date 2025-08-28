import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Yuri {


    private static final List<Task> tasks = new ArrayList<>();
    private static final Parser parser = new Parser();


    private static final int MAX_TASKS = 100;
    //private static final String[] tasks = new String[MAX_TASKS];
    //private static final Task[] tasks = new Task[MAX_TASKS];
    private static int taskCount = 0;

    //UI HELPERS
    private static final Ui ui = new Ui();
    // ADD/LIST/MARK/UNMARK
    private static final Storage storage = new Storage("data/duke.txt");


    private static void addTask(Task task) throws YuriException {
        if (task == null || task.description == null || task.description.trim().isEmpty()) {
            throw new YuriException("Task description cannot be empty.");
        }
        tasks.add(task);

        try {
            storage.save(tasks);
        } catch (java.io.IOException e) {
            ui.showError("Failed to save: " + e.getMessage());
        }

        new Yuri().ui.showAdded(task, tasks.size());
    }

    // (Kept for backward compatibility if you type free text; now treated as Todo)
//    private static void addTask(String description) {
//        addTask(new Todo(description));
//    }



    private static void handleMark(String line) throws YuriException {
        int idx = parser.parseIndexOrThrow(line, "mark");
        int i = idx - 1;
        requireValidIndex(i);
        tasks.get(i).mark();
        new Yuri().ui.showMark(tasks.get(i));
    }


    private static void handleUnmark(String line) throws YuriException {
        int idx = parser.parseIndexOrThrow(line, "unmark");
        int i = idx - 1;
        requireValidIndex(i);
        tasks.get(i).unmark();
        new Yuri().ui.showUnmark(tasks.get(i));
    }

    private static void handleDelete(String line) throws YuriException {
        int idx = parser.parseIndexOrThrow(line, "delete");
        int i = idx - 1;
        requireValidIndex(i);
        Task removed = tasks.remove(i);
        new Yuri().ui.showDeleted(removed, tasks.size());
    }


    //LEVEL 4 COMMANDS

    private static void handleTodo(String line) throws YuriException {
        String desc = parser.sliceAfter(line, "todo");
        if (desc.isBlank()) throw new YuriException("The description of a todo cannot be empty.");
        addTask(new Todo(desc));
    }

    private static void handleDeadline(String line) throws YuriException {
        String payload = parser.sliceAfter(line, "deadline");
        String[] parts = parser.splitOnceOrThrow(payload, "/by",
                "Deadline needs '/by <when>'. Example: deadline return book /by Sunday");
        String desc = parts[0].trim();
        String by = parts[1].trim();
        if (desc.isEmpty()) throw new YuriException("Deadline description cannot be empty.");
        if (by.isEmpty()) throw new YuriException("Please specify when the deadline is due after '/by'.");
        addTask(new Deadline(desc, by));
    }


    private static void handleEvent(String line) throws YuriException {
        String payload = parser.sliceAfter(line, "event");
        String[] pFrom = parser.splitOnceOrThrow(payload, "/from",
                "Event needs '/from <start>'. Example: event meeting /from Mon 2pm /to 3pm");
        String desc = pFrom[0].trim();
        String rest = pFrom[1].trim();
        String[] pTo = parser.splitOnceOrThrow(rest, "/to",
                "Event needs '/to <end>'. Example: event meeting /from Mon 2pm /to 3pm");
        String from = pTo[0].trim();
        String to = pTo[1].trim();
        if (desc.isEmpty()) throw new YuriException("Event description cannot be empty.");
        if (from.isEmpty()) throw new YuriException("Please specify the event start after '/from'.");
        if (to.isEmpty()) throw new YuriException("Please specify the event end after '/to'.");
        addTask(new Event(desc, from, to));
    }

    //HELPERS


    private static void requireValidIndex(int i) throws YuriException {
        if (i < 0 || i >= tasks.size()) {
            throw new YuriException("That task number doesn't exist yet. Try 'list' to see valid numbers.");
        }
    }

    //MAIN

    public static void main(String[] args) {

        try {
            ArrayList<Task> loaded = storage.load();
            tasks.addAll(loaded);
        } catch (IOException e) {
            ui.showError("Error loading save file: " + e.getMessage());
        }

        new Yuri().ui.showGreeting();

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                if (!sc.hasNextLine()) {
                    new Yuri().ui.showFarewell();
                    break;
                }
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                try {
                    if (line.toLowerCase().startsWith("list")) {
                        if (line.strip().contains(" ")) {
                            throw new YuriException("Just type 'list' with no extra words.");
                        }
                        new Yuri().ui.showList(tasks);
                    } else if (line.toLowerCase().startsWith("bye")) {
                        if (line.strip().contains(" ")) {
                            throw new YuriException("Just type 'bye' with no extra words.");
                        }
                        new Yuri().ui.showFarewell();
                        break;
                    } else if (parser.startsWithWord(line, "mark")) {
                        handleMark(line);
                    } else if (parser.startsWithWord(line, "unmark")) {
                        handleUnmark(line);
                    } else if (parser.startsWithWord(line, "delete")) {
                        handleDelete(line);
                    } else if (parser.startsWithWord(line, "todo")) {
                        handleTodo(line);
                    } else if (parser.startsWithWord(line, "deadline")) {
                        handleDeadline(line);
                    } else if (parser.startsWithWord(line, "event")) {
                        handleEvent(line);
                    } else {
                        throw new YuriException("I don’t recognize that command. Try: todo, deadline, event, list, mark, unmark, delete, bye.");
                    }
                } catch (YuriException e) {
                    ui.showError(e.getMessage());
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
        private final LocalDate by;

        Deadline(String description, String by) {
            super(description);
            this.by = LocalDate.parse(by); // input must be yyyy-MM-dd
        }

        @Override
        public String toString() {
            return "[D]" + super.toString() + " (by: "
                    + by.format(DateTimeFormatter.ofPattern("MMM d yyyy")) + ")";
        }

        @Override
        public String toSaveFormat() {
            return "D | " + (isDone ? "1" : "0") + " | "
                    + description + " | " + by; // saves as yyyy-MM-dd
        }
    }

    static class Event extends Task {
        private final LocalDate from;
        private final LocalDate to;

        Event(String description, String from, String to) {
            super(description);
            this.from = LocalDate.parse(from);
            this.to = LocalDate.parse(to);
        }

        @Override
        public String toString() {
            return "[E]" + super.toString() + " (from: "
                    + from.format(DateTimeFormatter.ofPattern("MMM d yyyy"))
                    + " to: " + to.format(DateTimeFormatter.ofPattern("MMM d yyyy")) + ")";
        }

        @Override
        public String toSaveFormat() {
            return "E | " + (isDone ? "1" : "0") + " | "
                    + description + " | " + from + " | " + to;
        }
    }


    //CUSTOM EXCEPTION

    static class YuriException extends Exception {
        YuriException(String message) {
            super(message);
        }
    }

}



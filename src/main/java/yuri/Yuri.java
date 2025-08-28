package yuri;

import java.io.IOException;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Yuri {

    private static final Parser parser = new Parser();
    private final TaskList tasks;;


    private static final int MAX_TASKS = 100;
    //private static final Task[] tasks = new Task[MAX_TASKS];
    private static int taskCount = 0;


    private static final Ui ui = new Ui();
    // ADD/LIST/MARK/UNMARK
    private static final Storage storage = new Storage("data/duke.txt");

    public Yuri() {
        TaskList loaded;
        try {
            loaded = new TaskList(storage.load()); // loads from file
        } catch (IOException e) {
            ui.showError("Error loading save file: " + e.getMessage());
            loaded = new TaskList(); // empty if load fails
        }
        this.tasks = loaded;
    }

    // (Kept for backward compatibility if you type free text; now treated as Todo)
//    private static void addTask(String description) {
//        addTask(new Todo(description));
//    }



    //LEVEL 4 COMMANDS


    //HELPERS

    //MAIN

//    public static void main(String[] args) {
//
//        try {
//            ArrayList<Task> loaded = storage.load();
//            tasks.addAll(loaded);
//        } catch (IOException e) {
//            ui.showError("Error loading save file: " + e.getMessage());
//        }
//
//        new yuri.Yuri().ui.showGreeting();
//
//        try (Scanner sc = new Scanner(System.in)) {
//            while (true) {
//                if (!sc.hasNextLine()) {
//                    new yuri.Yuri().ui.showFarewell();
//                    break;
//                }
//                String line = sc.nextLine().trim();
//                if (line.isEmpty()) continue;
//
//                try {
//                    if (line.toLowerCase().startsWith("list")) {
//                        if (line.strip().contains(" ")) {
//                            throw new YuriException("Just type 'list' with no extra words.");
//                        }
//                        new yuri.Yuri().ui.showList(tasks);
//                    } else if (line.toLowerCase().startsWith("bye")) {
//                        if (line.strip().contains(" ")) {
//                            throw new YuriException("Just type 'bye' with no extra words.");
//                        }
//                        new yuri.Yuri().ui.showFarewell();
//                        break;
//                    } else if (parser.startsWithWord(line, "mark")) {
//                        handleMark(line);
//                    } else if (parser.startsWithWord(line, "unmark")) {
//                        handleUnmark(line);
//                    } else if (parser.startsWithWord(line, "delete")) {
//                        handleDelete(line);
//                    } else if (parser.startsWithWord(line, "todo")) {
//                        handleTodo(line);
//                    } else if (parser.startsWithWord(line, "deadline")) {
//                        handleDeadline(line);
//                    } else if (parser.startsWithWord(line, "event")) {
//                        handleEvent(line);
//                    } else {
//                        throw new YuriException("I don’t recognize that command. Try: todo, deadline, event, list, mark, unmark, delete, bye.");
//                    }
//                } catch (YuriException e) {
//                    ui.showError(e.getMessage());
//                }
//            }
//        }
//    }

    public static void main(String[] args) {
        new Yuri().run();
    }


    public void run() {
        ui.showGreeting();
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                if (!sc.hasNextLine()) {
                    ui.showFarewell();
                    break;
                }
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                try {
                    if (parser.startsWithWord(line, "list")) {
                        if (line.strip().contains(" "))
                            throw new YuriException("Just type 'list' with no extra words.");
                        ui.showList(tasks.all());

                    } else if (parser.startsWithWord(line, "bye")) {
                        if (line.strip().contains(" "))
                            throw new YuriException("Just type 'bye' with no extra words.");
                        ui.showFarewell();
                        break;

                    } else if (parser.startsWithWord(line, "mark")) {
                        int idx = parser.parseIndexOrThrow(line, "mark") - 1;
                        requireValidIndex(idx);
                        tasks.mark(idx);
                        ui.showMark(tasks.get(idx));
                        persist();

                    } else if (parser.startsWithWord(line, "unmark")) {
                        int idx = parser.parseIndexOrThrow(line, "unmark") - 1;
                        requireValidIndex(idx);
                        tasks.unmark(idx);
                        ui.showUnmark(tasks.get(idx));
                        persist();

                    } else if (parser.startsWithWord(line, "delete")) {
                        int idx = parser.parseIndexOrThrow(line, "delete") - 1;
                        requireValidIndex(idx);
                        Yuri.Task removed = tasks.remove(idx);
                        ui.showDeleted(removed, tasks.size());
                        persist();

                    } else if (parser.startsWithWord(line, "todo")) {
                        String desc = parser.sliceAfter(line, "todo");
                        if (desc.isBlank())
                            throw new YuriException("The description of a todo cannot be empty.");
                        Yuri.Task t = new Todo(desc);
                        tasks.add(t);
                        ui.showAdded(t, tasks.size());
                        persist();

                    } else if (parser.startsWithWord(line, "deadline")) {
                        String payload = parser.sliceAfter(line, "deadline");
                        String[] parts = parser.splitOnceOrThrow(payload, "/by",
                                "Deadline needs '/by <when>'. Example: deadline return book /by 2019-12-02");
                        String desc = parts[0].trim();
                        String by = parts[1].trim();
                        if (desc.isEmpty())
                            throw new YuriException("Deadline description cannot be empty.");
                        if (by.isEmpty())
                            throw new YuriException("Please specify when the deadline is due after '/by'.");
                        Yuri.Task t = new Deadline(desc, by);
                        tasks.add(t);
                        ui.showAdded(t, tasks.size());
                        persist();

                    } else if (parser.startsWithWord(line, "event")) {
                        String payload = parser.sliceAfter(line, "event");
                        String[] pFrom = parser.splitOnceOrThrow(payload, "/from",
                                "Event needs '/from <start>'. Example: event meeting /from 2019-12-10 /to 2019-12-12");
                        String desc = pFrom[0].trim();
                        String rest = pFrom[1].trim();
                        String[] pTo = parser.splitOnceOrThrow(rest, "/to",
                                "Event needs '/to <end>'. Example: event meeting /from 2019-12-10 /to 2019-12-12");
                        String from = pTo[0].trim();
                        String to = pTo[1].trim();
                        if (desc.isEmpty()) throw new YuriException("Event description cannot be empty.");
                        if (from.isEmpty()) throw new YuriException("Please specify the event start after '/from'.");
                        if (to.isEmpty()) throw new YuriException("Please specify the event end after '/to'.");
                        Yuri.Task t = new Event(desc, from, to);
                        tasks.add(t);
                        ui.showAdded(t, tasks.size());
                        persist();

                    } else {
                        throw new YuriException("I don’t recognize that command. Try: todo, deadline, event, list, mark, unmark, delete, bye.");
                    }
                } catch (YuriException e) {
                    ui.showError(e.getMessage());
                }
            }
        }
    }

    private void requireValidIndex(int i) throws YuriException {
        if (i < 0 || i >= tasks.size()) {
            throw new YuriException("That task number doesn't exist yet. Try 'list' to see valid numbers.");
        }
    }

    private void persist() {
        try {
            storage.save(tasks.all());
        } catch (IOException e) {
            ui.showError("Failed to save: " + e.getMessage());
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



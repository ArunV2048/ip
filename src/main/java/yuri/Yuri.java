package yuri;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Entry point and main control loop for the application.
 * Wires together {@link Ui}, {@link Storage}, and {@link TaskList},
 * then processes user commands until exit.
 */
public class Yuri {

    private final Parser parser = new Parser();
    private final Ui ui = new Ui();
    private final Storage storage = new Storage("data/duke.txt");
    private final TaskList tasks;

    public String getGreeting() {
        return "Hello! I'm Yuri\nWhat can I do for you?\n"
                + "(Tip: todo, deadline, event, list, mark, unmark, delete, find, bye)";
    }

    /* Handles one user input and returns the response text for the GUI. */
    /**
     * Produces a single reply for one line of user input (used by GUI).
     * Also persists changes where relevant.
     */
    public String getResponse(String input) {
        try {
            String line = input.trim();
            if (line.isEmpty()) return "";

            if (parser.startsWithWord(line, "list")) {
                if (line.strip().contains(" ")) throw new YuriException("Just type 'list' with no extra words.");
                StringBuilder sb = new StringBuilder("Here are the tasks in your list:\n");
                java.util.List<Task> all = tasks.all();
                for (int i = 0; i < all.size(); i++) {
                    sb.append(" ").append(i + 1).append(".").append(all.get(i)).append("\n");
                }
                return sb.toString().trim();

            } else if (parser.startsWithWord(line, "bye")) {
                return "Bye. Hope to see you again soon!";

            } else if (parser.startsWithWord(line, "find")) {
                String keyword = parser.sliceAfter(line, "find");
                if (keyword.isBlank()) throw new YuriException("Please provide a keyword. Example: find book");
                java.util.List<Task> matches = tasks.find(keyword);
                if (matches.isEmpty()) return "No matching tasks found.";
                StringBuilder sb = new StringBuilder("Here are the matching tasks in your list:\n");
                for (int i = 0; i < matches.size(); i++) {
                    sb.append(" ").append(i + 1).append(".").append(matches.get(i)).append("\n");
                }
                return sb.toString().trim();

            } else if (parser.startsWithWord(line, "mark")) {
                int idx = parser.parseIndexOrThrow(line, "mark") - 1;
                requireValidIndex(idx);
                tasks.mark(idx);
                persist();
                return "Nice! I've marked this task as done:\n   " + tasks.get(idx);

            } else if (parser.startsWithWord(line, "unmark")) {
                int idx = parser.parseIndexOrThrow(line, "unmark") - 1;
                requireValidIndex(idx);
                tasks.unmark(idx);
                persist();
                return "OK, I've marked this task as not done yet:\n   " + tasks.get(idx);

            } else if (parser.startsWithWord(line, "delete")) {
                int idx = parser.parseIndexOrThrow(line, "delete") - 1;
                requireValidIndex(idx);
                Task removed = tasks.remove(idx);
                persist();
                return "Noted. I've removed this task:\n   " + removed + "\nNow you have " + tasks.size() + " tasks in the list.";

            } else if (parser.startsWithWord(line, "todo")) {
                String desc = parser.sliceAfter(line, "todo");
                if (desc.isBlank()) throw new YuriException("The description of a todo cannot be empty.");
                Task t = new Todo(desc);
                tasks.add(t);
                persist();
                return "Got it. I've added this task:\n   " + t + "\nNow you have " + tasks.size() + " tasks in the list.";

            } else if (parser.startsWithWord(line, "deadline")) {
                String payload = parser.sliceAfter(line, "deadline");
                String[] parts = parser.splitOnceOrThrow(payload, "/by",
                        "Deadline needs '/by <when>'. Example: deadline return book /by 2019-12-02");
                String desc = parts[0].trim();
                String by = parts[1].trim();
                if (desc.isEmpty()) throw new YuriException("Deadline description cannot be empty.");
                if (by.isEmpty()) throw new YuriException("Please specify when the deadline is due after '/by'.");
                Task t = new Deadline(desc, by);
                tasks.add(t);
                persist();
                return "Got it. I've added this task:\n   " + t + "\nNow you have " + tasks.size() + " tasks in the list.";

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
                Task t = new Event(desc, from, to);
                tasks.add(t);
                persist();
                return "Got it. I've added this task:\n   " + t + "\nNow you have " + tasks.size() + " tasks in the list.";
            }

            return "I don’t recognize that command. Try: todo, deadline, event, list, mark, unmark, delete, find, bye.";
        } catch (YuriException e) {
            return "OOPS!!! " + e.getMessage();
        }
    }

    /** Builds the numbered task list output (used by 'list'). */
    private String renderList() {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the tasks in your list:\n");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append(" ").append(i + 1).append(".").append(tasks.get(i)).append("\n");
        }
        return sb.toString().stripTrailing();
    }

    /** Builds the 'find' output. */
    private String renderFind(String keyword) {
        String kw = keyword.toLowerCase();
        StringBuilder sb = new StringBuilder("Here are the matching tasks in your list:\n");
        int shown = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            String d = t.getDescription();
            if (d != null && d.toLowerCase().contains(kw)) {
                sb.append(" ").append(i + 1).append(".").append(t).append("\n");
                shown++;
            }
        }
        return shown == 0 ? "No tasks match: " + keyword : sb.toString().stripTrailing();
    }


    /** Constructs the app, loading tasks from storage if available. */
    public Yuri() {
        TaskList loaded;
        try {
            loaded = new TaskList(storage.load());
        } catch (IOException e) {
            ui.showError("Error loading save file: " + e.getMessage());
            loaded = new TaskList();
        }
        this.tasks = loaded;
    }

    /**
     * Program entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        new Yuri().run();
    }

    /**
     * Runs the interactive command loop: reads a line, parses it, mutates state,
     * persists when necessary, and renders output via {@link Ui}. Exits on {@code bye} or EOF.
     */
    public void run() {
        ui.showGreeting();

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                if (!sc.hasNextLine()) {
                    ui.showFarewell();
                    break;
                }
                String line = sc.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    if (parser.startsWithWord(line, "list")) {
                        if (line.strip().contains(" ")) {
                            throw new YuriException("Just type 'list' with no extra words.");
                        }
                        ui.showList(tasks.all());

                    } else if (parser.startsWithWord(line, "bye")) {
                        if (line.strip().contains(" ")) {
                            throw new YuriException("Just type 'bye' with no extra words.");
                        }
                        ui.showFarewell();
                        break;

                    } else if (parser.startsWithWord(line, "find")) {
                        String keyword = parser.sliceAfter(line, "find");
                        if (keyword.isBlank()) {
                            throw new YuriException("Please provide a keyword. Example: find book");
                        }
                        java.util.List<Task> matches = tasks.find(keyword);
                        ui.showFindResults(matches);

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
                        Task removed = tasks.remove(idx);
                        ui.showDeleted(removed, tasks.size());
                        persist();

                    } else if (parser.startsWithWord(line, "todo")) {
                        String desc = parser.sliceAfter(line, "todo");
                        if (desc.isBlank()) {
                            throw new YuriException("The description of a todo cannot be empty.");
                        }
                        Task t = new Todo(desc);
                        tasks.add(t);
                        ui.showAdded(t, tasks.size());
                        persist();

                    } else if (parser.startsWithWord(line, "deadline")) {
                        String payload = parser.sliceAfter(line, "deadline");
                        String[] parts = parser.splitOnceOrThrow(payload, "/by",
                                "Deadline needs '/by <when>'. Example: deadline return book /by 2019-12-02");
                        String desc = parts[0].trim();
                        String by = parts[1].trim();
                        if (desc.isEmpty()) {
                            throw new YuriException("Deadline description cannot be empty.");
                        }
                        if (by.isEmpty()) {
                            throw new YuriException("Please specify when the deadline is due after '/by'.");
                        }
                        Task t = new Deadline(desc, by);
                        tasks.add(t);
                        ui.showAdded(t, tasks.size());
                        persist();

                    } else if (parser.startsWithWord(line, "event")) {
                        String payload = parser.sliceAfter(line, "event");
                        String[] pFrom = parser.splitOnceOrThrow(
                                payload,
                                "/from",
                                "Event needs '/from <start>'. Example: event meeting /from 2019-12-10 /to 2019-12-12"
                        );
                        String desc = pFrom[0].trim();
                        String rest = pFrom[1].trim();
                        String[] pTo = parser.splitOnceOrThrow(
                                rest,
                                "/to",
                                "Event needs '/to <end>'. Example: event meeting /from 2019-12-10 /to 2019-12-12"
                        );
                        String from = pTo[0].trim();
                        String to = pTo[1].trim();

                        if (desc.isEmpty()) {
                            throw new YuriException("Event description cannot be empty.");
                        }
                        if (from.isEmpty()) {
                            throw new YuriException("Please specify the event start after '/from'.");
                        }
                        if (to.isEmpty()) {
                            throw new YuriException("Please specify the event end after '/to'.");
                        }

                        Task t = new Event(desc, from, to);
                        tasks.add(t);
                        ui.showAdded(t, tasks.size());
                        persist();

                    } else {
                        throw new YuriException(
                                "I don’t recognize that command. Try: todo, deadline, event, list, mark, unmark, delete, bye."
                        );
                    }
                } catch (YuriException e) {
                    ui.showError(e.getMessage());
                }
            }
        }
    }

    /**
     * Ensures the given zero-based index refers to an existing task.
     *
     * @param i zero-based index to check
     * @throws YuriException if the index is invalid
     */
    private void requireValidIndex(int i) throws YuriException {
        if (i < 0 || i >= tasks.size()) {
            throw new YuriException("That task number doesn't exist yet. Try 'list' to see valid numbers.");
        }
    }

    /** Persists the current task list to storage, reporting any I/O errors via {@link Ui}. */
    private void persist() {
        try {
            storage.save(tasks.all());
        } catch (IOException e) {
            ui.showError("Failed to save: " + e.getMessage());
        }
    }

    /* =========================
       Task model + subclasses
       ========================= */

    /** Represents a generic task with a description and done state. */
    static class Task {

        protected final String description;
        protected boolean isDone;

        Task(String description) {
            this.description = description;
            this.isDone = false;
        }

        void mark() {
            this.isDone = true;
        }

        void unmark() {
            this.isDone = false;
        }

        String getStatusIcon() {
            return isDone ? "X" : " ";
        }

        /** Returns the description of this task. */
        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "[" + getStatusIcon() + "] " + description;
        }

        /** Returns the save format, defaulting to a Todo-like encoding. */
        public String toSaveFormat() {
            return "T | " + (isDone ? "1" : "0") + " | " + description;
        }
    }

    /** A Todo task with only a description. */
    static class Todo extends Task {
        Todo(String description) {
            super(description);
        }

        @Override
        public String toString() {
            return "[T]" + super.toString();
        }

        @Override
        public String toSaveFormat() {
            return "T | " + (isDone ? "1" : "0") + " | " + description;
        }
    }

    /** A Deadline task with a due date (yyyy-MM-dd). */
    static class Deadline extends Task {

        private final LocalDate by;

        Deadline(String description, String by) {
            super(description);
            this.by = LocalDate.parse(by);
        }

        @Override
        public String toString() {
            return "[D]" + super.toString()
                    + " (by: " + by.format(DateTimeFormatter.ofPattern("MMM d yyyy")) + ")";
        }

        @Override
        public String toSaveFormat() {
            return "D | " + (isDone ? "1" : "0") + " | " + description + " | " + by;
        }
    }

    /** An Event task with start and end dates (yyyy-MM-dd). */
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
            return "[E]" + super.toString()
                    + " (from: " + from.format(DateTimeFormatter.ofPattern("MMM d yyyy"))
                    + " to: " + to.format(DateTimeFormatter.ofPattern("MMM d yyyy")) + ")";
        }

        @Override
        public String toSaveFormat() {
            return "E | " + (isDone ? "1" : "0") + " | " + description + " | " + from + " | " + to;
        }
    }

    /** Dedicated checked exception for user-facing errors. */
    static class YuriException extends Exception {
        YuriException(String message) {
            super(message);
        }
    }
}

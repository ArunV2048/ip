package yuri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds and manages the list of tasks in memory.
 * Provides basic operations such as add, remove, mark, and unmark.
 */
public class TaskList {

    private final List<Yuri.Task> tasks;

    /** Constructs an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /** Constructs a task list initialized with the given tasks. */
    public TaskList(List<Yuri.Task> initial) {
        this.tasks = new ArrayList<>(initial);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param t task to add
     */
    public void add(Yuri.Task t) {
        tasks.add(t);
    }

    /**
     * Returns the task at the given zero-based index.
     *
     * @param idx0 zero-based index of the task
     * @return the task at {@code idx0}
     * @throws IndexOutOfBoundsException if {@code idx0} is out of range
     */
    public Yuri.Task get(int idx0) {
        return tasks.get(idx0);
    }

    /**
     * Removes and returns the task at the given zero-based index.
     *
     * @param idx0 zero-based index of the task to remove
     * @return the removed task
     * @throws IndexOutOfBoundsException if {@code idx0} is out of range
     */
    public Yuri.Task remove(int idx0) {
        return tasks.remove(idx0);
    }

    /**
     * Marks the task at the given zero-based index as done.
     *
     * @param idx0 zero-based index of the task to mark
     * @throws IndexOutOfBoundsException if {@code idx0} is out of range
     */
    public void mark(int idx0) {
        tasks.get(idx0).mark();
    }

    /**
     * Marks the task at the given zero-based index as not done.
     *
     * @param idx0 zero-based index of the task to unmark
     * @throws IndexOutOfBoundsException if {@code idx0} is out of range
     */
    public void unmark(int idx0) {
        tasks.get(idx0).unmark();
    }

    /**
     * Returns all tasks whose description contains the given keyword (case-insensitive).
     *
     * @param keyword keyword to search for
     * @return list of matching tasks (possibly empty)
     * @throws IllegalArgumentException if keyword is null or blank
     */
    public java.util.List<Yuri.Task> find(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty.");
        }
        String needle = keyword.toLowerCase();
        java.util.List<Yuri.Task> results = new java.util.ArrayList<>();
        for (Yuri.Task t : tasks) {
            String desc = t.getDescription();
            if (desc != null && desc.toLowerCase().contains(needle)) {
                results.add(t);
            }
        }
        return results;
    }

    /**
     * Returns the number of tasks currently in the list.
     *
     * @return task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns an unmodifiable view of the tasks.
     *
     * @return unmodifiable list view of tasks
     */
    public List<Yuri.Task> all() {
        return Collections.unmodifiableList(tasks);
    }
}

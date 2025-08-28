package yuri;

import java.util.List;

/** Handles all user-facing output for the CLI (greeting, lists, confirmations, errors).
 *  Centralizes formatting so core logic remains clean.
 */
public class Ui {
    private static final String BOT_NAME = "yuri.Yuri";
    private static final String HLINE = "____________________________________________________________";

    private void line() { System.out.println(HLINE); }

    /** Prints the greeting header shown at program start. */
    public void showGreeting() {
        line();
        System.out.println(" Hello! I'm " + BOT_NAME);
        System.out.println(" What can I do for you?");
        System.out.println(" (Tip: type anything to add; 'list' to see; 'mark n'/'unmark n'; 'bye' to exit.)");
        line();
    }

    /** Prints the farewell message and closing line. */
    public void showFarewell() {
        line();
        System.out.println(" Bye. Hope to see you again soon!");
        System.out.println(" That was fun—high five for productivity!");
        line();
    }

    /** Prints a formatted error message in a consistent style.
     *  @param message error details to show
     */
    public void showError(String message) {
        line();
        System.out.println(" OOPS!!! " + message);
        line();
    }

    /** Prints a confirmation that a task was added, and shows the new list size.
     *  @param task the task that was added
     *  @param newSize resulting size of the task list
     */
    public void showAdded(Yuri.Task task, int newSize) {
        line();
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + newSize + " tasks in the list.");
        line();
    }


    public void showDeleted(Yuri.Task removed, int newSize) {
        line();
        System.out.println(" Noted. I've removed this task:");
        System.out.println("   " + removed);
        System.out.println(" Now you have " + newSize + " tasks in the list.");
        line();
    }

    /** Prints a confirmation that a task was marked done.
     *  @param t the task that was marked
     */
    public void showMark(Yuri.Task t) {
        line();
        System.out.println(" Nice! I've marked this task as done:");
        System.out.println("   " + t);
        line();
    }

    /** Prints a confirmation that a task was marked not done.
     *  @param t the task that was unmarked
     */
    public void showUnmark(Yuri.Task t) {
        line();
        System.out.println(" OK, I've marked this task as not done yet:");
        System.out.println("   " + t);
        line();
    }

    /** Prints the current list of tasks with 1-based numbering.
     *  @param tasks list view of tasks to display
     */
    public void showList(List<Yuri.Task> tasks) {
        line();
        System.out.println(" Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + "." + tasks.get(i));
        }
        line();
    }
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskList {
    private final List<Yuri.Task> tasks;

    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    public TaskList(List<Yuri.Task> initial) {
        this.tasks = new ArrayList<>(initial);
    }

    public void add(Yuri.Task t) {
        tasks.add(t);
    }

    public Yuri.Task get(int idx0) {
        return tasks.get(idx0);
    }

    public Yuri.Task remove(int idx0) {
        return tasks.remove(idx0);
    }

    public void mark(int idx0) {
        tasks.get(idx0).mark();
    }

    public void unmark(int idx0) {
        tasks.get(idx0).unmark();
    }

    public int size() {
        return tasks.size();
    }

    public List<Yuri.Task> all() {
        return Collections.unmodifiableList(tasks);
    }
}

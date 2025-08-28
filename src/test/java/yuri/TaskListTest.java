package yuri;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskListTest {

    private TaskList tasks;

    @BeforeEach
    void setup() {
        tasks = new TaskList();
    }

    @Test
    void add_and_get_increasesSize_andStoresTask() {
        Yuri.Task t = new Yuri.Todo("read book");
        tasks.add(t);
        assertEquals(1, tasks.size());
        assertSame(t, tasks.get(0));
    }

    @Test
    void remove_returnsRemoved_andShrinks() {
        Yuri.Task t1 = new Yuri.Todo("A");
        Yuri.Task t2 = new Yuri.Todo("B");
        tasks.add(t1);
        tasks.add(t2);
        Yuri.Task removed = tasks.remove(0);
        assertSame(t1, removed);
        assertEquals(1, tasks.size());
        assertSame(t2, tasks.get(0));
    }

    @Test
    void mark_unmark_toggleStatus() {
        Yuri.Task t = new Yuri.Todo("Task");
        tasks.add(t);
        tasks.mark(0);
        assertTrue(tasks.get(0).isDone);
        tasks.unmark(0);
        assertFalse(tasks.get(0).isDone);
    }

    @Test
    void all_returnsUnmodifiableView() {
        Yuri.Task t = new Yuri.Todo("X");
        tasks.add(t);
        List<Yuri.Task> view = tasks.all();
        assertEquals(1, view.size());
        assertThrows(UnsupportedOperationException.class, () -> view.add(t));
    }
}

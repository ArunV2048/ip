package yuri;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Handles persistence of tasks to and from the save file on disk.
 * The file format is the simple pipe-separated format produced by each task's {@code toSaveFormat()}.
 */
public class Storage {

    private final String filePath;

    /**
     * Creates a {@code Storage} that reads/writes to the given path.
     *
     * @param filePath path of the save file, e.g. {@code data/duke.txt}
     */
    public Storage(String filePath) {
        assert filePath != null : "Storage filePath must not be null";
        this.filePath = filePath;
    }

    /**
     * Loads tasks from disk.
     * <p>If the file does not exist, it is created and an empty list is returned.</p>
     *
     * @return list of tasks loaded from disk (possibly empty)
     * @throws IOException if an I/O error occurs while creating/reading the file
     */
    public ArrayList<Yuri.Task> load() throws IOException {
        ArrayList<Yuri.Task> tasks = new ArrayList<>();
        File file = new File(filePath);
        assert file != null : "File ref must not be null";

        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            boolean created = file.createNewFile();
            assert file.exists() || created : "File should exist or be created";
            return tasks;
        }

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                assert line != null : "Read line should not be null";
                Yuri.Task task = parseTask(line);
                // task may be null for malformed lines by design
                if (task != null) {
                    tasks.add(task);
                }
            }
        }
        assert tasks != null : "Tasks list should not be null";
        return tasks;
    }

    /**
     * Saves the given tasks to disk, overwriting any existing content.
     *
     * @param tasks tasks to persist
     * @throws IOException if an I/O error occurs while writing
     */
    public void save(List<Yuri.Task> tasks) throws IOException {
        assert tasks != null : "tasks to save must not be null";
        try (FileWriter fw = new FileWriter(filePath)) {
            for (Yuri.Task task : tasks) {
                assert task != null : "Individual task must not be null";
                String line = task.toSaveFormat();
                assert line != null : "toSaveFormat must not return null";
                fw.write(line);
                fw.write(System.lineSeparator());
            }
        }
    }

    /* =========================
       Internal: parse a task line
       ========================= */

    private Yuri.Task parseTask(String line) {
        assert line != null : "parseTask input line must not be null";
        // Expected formats:
        // T | 0/1 | description
        // D | 0/1 | description | yyyy-MM-dd
        // E | 0/1 | description | yyyy-MM-dd | yyyy-MM-dd
        String[] p = line.split("\\s*\\|\\s*");
        if (p.length < 3) {
            return null;
        }

        String type = p[0];
        assert type != null && !type.isEmpty() : "Task type must be present";
        boolean done = "1".equals(p[1]);

        Yuri.Task t;
        switch (type) {
            case "T":
                t = new Yuri.Todo(p[2]);
                break;
            case "D":
                assert p.length >= 4 : "Deadline requires date part";
                t = new Yuri.Deadline(p[2], p[3]);
                break;
            case "E":
                assert p.length >= 5 : "Event requires start and end dates";
                t = new Yuri.Event(p[2], p[3], p[4]);
                break;
            default:
                return null;
        }

        assert t != null : "Constructed task should not be null";
        if (done) {
            t.mark();
        }
        return t;
    }
}

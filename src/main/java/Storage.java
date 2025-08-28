import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Storage {
    private final String filePath;

    public Storage(String filePath) { this.filePath = filePath; }

    public ArrayList<Yuri.Task> load() throws IOException {
        ArrayList<Yuri.Task> tasks = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            file.createNewFile();
            return tasks;
        }

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                Yuri.Task t = parseTask(sc.nextLine());
                if (t != null) tasks.add(t);
            }
        }
        return tasks;
    }

    public void save(List<Yuri.Task> tasks) throws IOException {
        try (FileWriter fw = new FileWriter(filePath)) {
            for (Yuri.Task t : tasks) {
                fw.write(t.toSaveFormat());
                fw.write(System.lineSeparator());
            }
        }
    }

    // Parse your on-disk format
    private Yuri.Task parseTask(String line) {
        String[] p = line.split("\\s*\\|\\s*");
        if (p.length < 3) return null;
        String type = p[0];
        boolean done = "1".equals(p[1]);
        Yuri.Task t;
        switch (type) {
            case "T":
                t = new Yuri.Todo(p[2]);
                break;
            case "D":
                t = new Yuri.Deadline(p[2], p[3]);
                break;
            case "E":
                t = new Yuri.Event(p[2], p[3], p[4]);
                break;
            default:
                return null;
        }
        if (done) t.mark();
        return t;
    }
}


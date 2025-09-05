package yuri;

/**
 * Entry point that launches JavaFX without tripping up classloader/module issues.
 */
public class Launcher {
    /**
     * Starts the JavaFX application.
     *
     * @param args CLI args
     */
    public static void main(String[] args) {
        yuri.gui.Main.main(args);
    }
}

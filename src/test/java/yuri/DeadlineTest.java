package yuri;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeadlineTest {

    @Test
    void toString_formatsDate_nicely() {
        Yuri.Deadline d = new Yuri.Deadline("return book", "2019-12-02");
        String s = d.toString();
        assertTrue(s.contains("return book"));
        assertTrue(s.contains("Dec 2 2019")); // matches your DateTimeFormatter "MMM d yyyy"
    }

    @Test
    void toSaveFormat_serializesInIso() {
        Yuri.Deadline d = new Yuri.Deadline("finish", "2020-01-05");
        String s = d.toSaveFormat();
        assertTrue(s.contains("D | 0 | finish | 2020-01-05"));
    }
}

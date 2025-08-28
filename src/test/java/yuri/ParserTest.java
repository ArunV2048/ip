package yuri;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ParserTest {

    private final Parser parser = new Parser();

    @Test
    void startsWithWord_exactMatch_true() {
        assertTrue(parser.startsWithWord("list", "list"));
    }

    @Test
    void startsWithWord_prefixWordOnly_true() {
        assertTrue(parser.startsWithWord("list all", "list"));
    }

    @Test
    void startsWithWord_partialWord_false() {
        assertFalse(parser.startsWithWord("listener", "list")); // not a whole word
    }

    @Test
    void parseIndexOrThrow_validNumber_ok() throws Yuri.YuriException {
        int idx = parser.parseIndexOrThrow("mark 3", "mark");
        assertEquals(3, idx);
    }

    @Test
    void parseIndexOrThrow_missingNumber_throws() {
        Yuri.YuriException ex = assertThrows(Yuri.YuriException.class,
                () -> parser.parseIndexOrThrow("mark", "mark"));
        assertTrue(ex.getMessage().contains("exactly one number"));
    }

    @Test
    void parseIndexOrThrow_nonPositive_throws() {
        assertThrows(Yuri.YuriException.class,
                () -> parser.parseIndexOrThrow("mark 0", "mark"));
        assertThrows(Yuri.YuriException.class,
                () -> parser.parseIndexOrThrow("mark -2", "mark"));
    }
}

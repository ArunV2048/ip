package yuri;

/**
 * Utility methods for interpreting raw user input lines.
 * Provides helpers to detect commands, slice arguments, and validate indices.
 */
public class Parser {

    /**
     * Returns true if {@code line} starts with the whole word {@code word} (case-insensitive).
     * Accepts either an exact match or a leading word followed by a space.
     *
     * @param line full user input
     * @param word command word to match
     * @return true if the first word matches; false otherwise
     */
    public boolean startsWithWord(String line, String word) {
        assert line != null && word != null : "Null inputs to startsWithWord";
        String lw = line.toLowerCase();
        String ww = word.toLowerCase();
        boolean result = lw.equals(ww) || lw.startsWith(ww + " ");
        assert (result || !lw.startsWith(word)) || lw.startsWith(ww) : "Case-insensitive check should align";
        return result;
    }

    /**
     * Returns the substring of {@code line} after the head command word.
     * Trims surrounding whitespace; returns empty string if nothing remains.
     *
     * @param line     full user input
     * @param headWord the leading command word
     * @return the argument substring after the command word (trimmed), or empty string
     */
    public String sliceAfter(String line, String headWord) {
        assert line != null && headWord != null : "Null inputs to sliceAfter";
        String trimmed = line.trim();
        if (trimmed.length() <= headWord.length()) {
            return "";
        }
        String out = trimmed.substring(headWord.length()).trim();
        assert out != null : "sliceAfter: result should not be null";
        return out;
    }

    /**
     * Splits {@code s} once around the first occurrence of {@code token}.
     * Returns a 2-element array {@code [left, right]} with {@code right} trimmed.
     *
     * @param s      the string to split
     * @param token  the delimiter token (case-insensitive, as implemented)
     * @param errMsg error message to use if the token is not found
     * @return two parts: left of the token, and right of the token (trimmed)
     * @throws yuri.Yuri.YuriException if the token cannot be found
     */
    public String[] splitOnceOrThrow(String s, String token, String errMsg) throws Yuri.YuriException {
        assert s != null && token != null && errMsg != null : "Null inputs to splitOnceOrThrow";
        int pos = indexOfToken(s, token);
        if (pos < 0) {
            throw new Yuri.YuriException(errMsg);
        }
        String left = s.substring(0, pos);
        String right = s.substring(pos + token.length()).trim();
        assert left != null && right != null : "Split parts should be non-null";
        return new String[]{left, right};
    }

    /**
     * Finds {@code token} in {@code s} case-insensitively; tolerates a leading space before token.
     *
     * @param s     the string to search
     * @param token the token
     * @return index of match, or -1 if not found
     */
    public int indexOfToken(String s, String token) {
        assert s != null && token != null : "Null inputs to indexOfToken";
        String low = s.toLowerCase();
        String t = token.toLowerCase();

        int i = low.indexOf(t);
        if (i >= 0) {
            assert s.regionMatches(true, i, token, 0, token.length()) : "Case-insensitive match must align";
            return i;
        }
        i = low.indexOf(" " + t);
        if (i >= 0) {
            int j = i + 1;
            assert s.regionMatches(true, j, token, 0, token.length()) : "Case-insensitive match must align (spaced)";
            return j;
        }
        return -1;
    }

    /**
     * Parses and returns a 1-based index from a command of the form {@code "<cmd> <number>"}.
     * Must have exactly one numeric argument.
     *
     * @param line the full command line
     * @param cmd  the command word (used only for error messages)
     * @return the parsed positive index (1-based)
     * @throws yuri.Yuri.YuriException if format is wrong or the number is not a positive integer
     */
    public int parseIndexOrThrow(String line, String cmd) throws Yuri.YuriException {
        assert line != null && cmd != null : "Null inputs to parseIndexOrThrow";

        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            throw new Yuri.YuriException("Use: '" + cmd + " <number>' with exactly one number.");
        }
        try {
            int idx = Integer.parseInt(parts[1]);
            assert idx > 0 : "Parsed index must be positive";
            if (idx <= 0) {
                throw new NumberFormatException();
            }
            return idx;
        } catch (NumberFormatException e) {
            throw new Yuri.YuriException("Index must be a positive number. Example: '" + cmd + " 2'.");
        }
    }
}

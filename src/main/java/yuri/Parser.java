package yuri;

public class Parser {
    public boolean startsWithWord(String line, String word) {
        String lw = line.toLowerCase();
        String ww = word.toLowerCase();
        return lw.equals(ww) || lw.startsWith(ww + " ");
    }

    public String sliceAfter(String line, String headWord) {
        String trimmed = line.trim();
        if (trimmed.length() <= headWord.length()) return "";
        return trimmed.substring(headWord.length()).trim();
    }

    public String[] splitOnceOrThrow(String s, String token, String errMsg) throws Yuri.YuriException {
        int pos = indexOfToken(s, token);
        if (pos < 0) throw new Yuri.YuriException(errMsg);
        String left = s.substring(0, pos);
        String right = s.substring(pos + token.length()).trim();
        return new String[] { left, right };
    }

    private int indexOfToken(String s, String token) {
        String low = s.toLowerCase();
        String t = token.toLowerCase();
        int i = low.indexOf(t);
        if (i >= 0) return i;
        i = low.indexOf(" " + t);
        if (i >= 0) return i + 1;
        return -1;
    }

    public int parseIndexOrThrow(String line, String cmd) throws Yuri.YuriException {
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            throw new Yuri.YuriException("Use: '" + cmd + " <number>' with exactly one number.");
        }
        try {
            int idx = Integer.parseInt(parts[1]);
            if (idx <= 0) throw new NumberFormatException();
            return idx;
        } catch (NumberFormatException e) {
            throw new Yuri.YuriException("Index must be a positive number. Example: '" + cmd + " 2'.");
        }
    }
}


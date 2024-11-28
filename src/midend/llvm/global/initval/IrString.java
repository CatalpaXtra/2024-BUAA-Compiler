package midend.llvm.global.initval;

public class IrString extends InitVal {
    private final String string;
    private final int size;

    public IrString(String string, int size) {
        this.string = string.substring(1, string.length() - 1);
        this.size = size;
    }

    public int getCharAt(int loc) {
        int len = 0;
        for (int i = 0; i < string.length(); i++, len++) {
            if (len == loc) {
                if (string.charAt(i) == '\\' && i + 1 < string.length()) {
                    return '\n';
                } else {
                    return string.charAt(i);
                }
            }
            if (string.charAt(i) == '\\') {
                i++;
            }
        }
        return '\0';
    }

    public String getStringConst() {
        return string;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("\"");
        int len = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '\\' && i + 1 < string.length()) {
                char next = string.charAt(i + 1);
                if (next == '0') {
                    sb.append("\\00");
                } else if (next == 'n') {
                    sb.append("\\0A");
                }
                i++;
            } else {
                sb.append(string.charAt(i));
            }
            len++;
        }
        sb.append("\\00".repeat(Math.max(0, size - len))).append("\"");
        return sb.toString();
    }
}

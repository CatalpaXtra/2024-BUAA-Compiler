package midend.llvm.global.initval;

public class IrString extends InitVal {
    private final String stringConst;
    private final int size;

    public IrString(String stringConst, int size) {
        this.stringConst = stringConst;
        this.size = size;
    }

    public int getCharAt(int loc) {
        return stringConst.charAt(loc);
    }

    public String toString() {
        String initVal = stringConst.substring(1, stringConst.length() - 1);
        String arrayFormat = "\"";
        int len = 0;
        for (int i = 0; i < initVal.length(); i++) {
            if (initVal.charAt(i) == '\\' && i + 1 < initVal.length()) {
                char next = initVal.charAt(i + 1);
                if (next == '0') {
                    arrayFormat += "\\00";
                } else if (next == 'n') {
                    arrayFormat += "\\0A";
                }
                i++;
            } else {
                arrayFormat += initVal.charAt(i);
            }
            len++;
        }
        for (int i = len; i < size; i++) {
            arrayFormat += "\\00";
        }
        return arrayFormat + "\"";
    }
}

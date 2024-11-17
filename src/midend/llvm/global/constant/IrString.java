package midend.llvm.global.constant;

public class IrString extends IrConstant {
    private final String stringConst;
    private final int size;

    public IrString(String stringConst, int size) {
        this.stringConst = stringConst;
        this.size = size;
    }

    public String irOut() {
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

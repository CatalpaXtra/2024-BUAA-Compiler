package midend.llvm;

public class RetValue {
    /* mode == 0, return value; mode == 1, return register; mode == 2, many in cond, no need return */
    private final int mode;
    private final int value;

    public RetValue(int value, int mode) {
        this.value = value;
        this.mode = mode;
    }

    public boolean isDigit() {
        return mode == 0;
    }

    public boolean isReg() {
        return mode == 1;
    }

    public int getValue() {
        return value;
    }

    public String irOut() {
        if (mode == 0) {
            return value + "";
        } else {
            return "%" + value;
        }
    }
}

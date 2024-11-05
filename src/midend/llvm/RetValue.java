package midend.llvm;

public class RetValue {
    private int regNum;
    private int value;
    private boolean isDigit;

    public RetValue(int value, boolean isDigit) {
        if (isDigit) {
            this.regNum = -1;
            this.value = value;
        } else {
            this.regNum = value;
            this.value = -1;
        }
        this.isDigit = isDigit;
    }

    public boolean isDigit() {
        return isDigit;
    }

    public int getValue() {
        return value;
    }

    public String getReg() {
        return "" + regNum;
    }

    public String irOut() {
        if (isDigit) {
            return value + "";
        } else {
            return "%" + regNum;
        }
    }
}

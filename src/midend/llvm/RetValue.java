package midend.llvm;

public class RetValue {
    private int regNum;
    private int value;
    private boolean isDigit;
    private boolean isMany;

    public RetValue(int value, boolean isDigit) {
        if (isDigit) {
            this.regNum = -1;
            this.value = value;
        } else {
            this.regNum = value;
            this.value = -1;
        }
        this.isDigit = isDigit;
        this.isMany = false;
    }

    public RetValue(int value, boolean isDigit, boolean isMany) {
        this.regNum = value;
        this.isDigit = isDigit;
        this.isMany = isMany;
    }

    public boolean isDigit() {
        return isDigit;
    }

    public boolean isReg() {
        return !isMany && !isDigit;
    }

    public int getValue() {
        return value;
    }

    public String irOut() {
        if (isDigit) {
            return value + "";
        } else {
            return "%" + regNum;
        }
    }
}

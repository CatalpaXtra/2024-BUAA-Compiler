package midend.llvm.global;

public class GlobalStr {
    private static int strNum = 0;
    private final String name;
    private final String string;
    private final int strLen;

    public GlobalStr(String string, int strLen) {
        this.string = string;
        this.strLen = strLen;
        if (strNum == 0) {
            this.name = "@.str";
        } else {
            this.name = "@.str." + strNum;
        }
        strNum++;
    }

    public String getName() {
        return name;
    }

    public String irOut() {
        return name + " = private unnamed_addr constant [" + strLen + " x i8] c\"" + string + "\", align 1";
    }
}

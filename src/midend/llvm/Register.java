package midend.llvm;

public class Register {
    private static int regNum;

    public Register() {
        regNum = 0;
    }

    public static void resetReg() {
        regNum = 0;
    }

    public static int allocReg() {
        return regNum++;
    }

    public static int cancelReg() {
        return regNum--;
    }
}

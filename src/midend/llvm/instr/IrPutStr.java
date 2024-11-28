package midend.llvm.instr;

public class IrPutStr extends IrInstr {
    private final String strName;
    private final int len;

    public IrPutStr(String strName, int len) {
        super(null, "void");
        this.strName = strName;
        this.len = len;
    }

    public String getStrName() {
        return strName.substring(1);
    }

    public String toString() {
        String rParams = "i8* getelementptr inbounds ([" + len + " x i8], [" + len + " x i8]* " + strName + ", i64 0, i64 0)";
        String instr = "call void @putstr(" + rParams + ")";
        return instr;
    }
}

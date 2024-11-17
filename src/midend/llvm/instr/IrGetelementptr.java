package midend.llvm.instr;

import midend.llvm.RetValue;

public class IrGetelementptr extends IrInstr {
    /* %1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3 */
    /* %3 = getelementptr i32, i32* %2, i32 3 */
    private final RetValue result;
    private final String irType;
    private final String pointer;
    private final String offset;
    private final int size;

    public IrGetelementptr(RetValue result, String irType, String pointer, String offset, int size) {
        this.result = result;
        this.irType = irType;
        this.pointer = pointer;
        this.offset = offset;
        this.size = size;
    }

    public String irOut() {
        String instr;
        if (size == -1) {
            instr = result.irOut() + " = getelementptr inbounds " + irType + ", " + irType + "* " + pointer + ", i32 " + offset;
        } else {
            instr = result.irOut() + " = getelementptr inbounds [" + size + " x " + irType + "]";
            instr += ", [" + size + " x " + irType + "]* " + pointer + ", i32 0, i32 " + offset;
        }
        return instr;
    }
}

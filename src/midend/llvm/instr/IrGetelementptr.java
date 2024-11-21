package midend.llvm.instr;

import midend.llvm.Value;

public class IrGetelementptr extends IrInstr {
    /* %1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3 */
    /* %3 = getelementptr i32, i32* %2, i32 3 */
    private final Value result;
    private final String irType;
    private final Value pointer;
    private final Value offset;
    private final int size;

    public IrGetelementptr(Value result, String irType, Value pointer, Value offset, int size) {
        super(result.irOut(), irType);
        this.result = result;
        this.irType = irType;
        this.pointer = pointer;
        this.offset = offset;
        this.size = size;
    }

    public String irOut() {
        String instr;
        if (size == -1) {
            instr = result.irOut() + " = getelementptr inbounds " + irType + ", " + irType + "* " + pointer.irOut() + ", i32 " + offset.irOut();
        } else {
            instr = result.irOut() + " = getelementptr inbounds [" + size + " x " + irType + "]";
            instr += ", [" + size + " x " + irType + "]* " + pointer.irOut() + ", i32 0, i32 " + offset.irOut();
        }
        return instr;
    }
}

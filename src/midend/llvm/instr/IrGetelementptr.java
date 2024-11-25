package midend.llvm.instr;

import midend.llvm.Value;

public class IrGetelementptr extends IrInstr {
    /* %1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3 */
    /* %3 = getelementptr i32, i32* %2, i32 3 */
    private final Value pointer;
    private final Value offset;
    private final int size;

    public IrGetelementptr(String name, String irType, Value pointer, Value offset, int size) {
        super(name, irType);
        this.pointer = pointer;
        this.offset = offset;
        this.size = size;
    }

    public String toString() {
        String instr;
        if (size == -1) {
            instr = name + " = getelementptr inbounds " + irType + ", " + irType + "* " + pointer.getName() + ", i32 " + offset.getName();
        } else {
            instr = name + " = getelementptr inbounds [" + size + " x " + irType + "]";
            instr += ", [" + size + " x " + irType + "]* " + pointer.getName() + ", i32 0, i32 " + offset.getName();
        }
        return instr;
    }
}

package midend.llvm.instr;

import midend.llvm.Value;

public class IrTrunc extends IrInstr{
    /* <result> = trunc <ty> <value> to <ty2> */
    private final String ty1;
    private final Value value;
    private final String ty2;

    public IrTrunc(String name, String ty1, Value value, String ty2) {
        super(name, ty2);
        this.ty1 = ty1;
        this.value = value;
        this.ty2 = ty2;
        addOperand(value);
    }

    public String toString() {
        String instr = name + " = trunc " + ty1 + " " + value.getName() + " to " + ty2;
        return instr;
    }
}

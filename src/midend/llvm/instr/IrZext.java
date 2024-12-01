package midend.llvm.instr;

import midend.llvm.Value;

public class IrZext extends IrInstr{
    /* <result> = zext <ty> <value> to <ty2> */
    private final String ty1;
    private final Value value;
    private final String ty2;

    public IrZext(String name, String ty1, Value value, String ty2) {
        super(name, ty2);
        this.ty1 = ty1;
        this.value = value;
        this.ty2 = ty2;
        addOperand(value);
    }

    public String getTy1() {
        return ty1;
    }

    public String getTy2() {
        return ty2;
    }

    public String toString() {
        String instr = name + " = zext " + ty1 + " " + value.getName() + " to " + ty2;
        return instr;
    }
}

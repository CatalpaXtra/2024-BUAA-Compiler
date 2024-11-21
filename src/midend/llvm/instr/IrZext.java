package midend.llvm.instr;

import midend.llvm.Value;

public class IrZext extends IrInstr{
    /* <result> = zext <ty> <value> to <ty2> */
    private final Value result;
    private final String ty1;
    private final Value value;
    private final String ty2;

    public IrZext(Value result, String ty1, Value value, String ty2) {
        super(result.irOut(), ty2);
        this.result = result;
        this.ty1 = ty1;
        this.value = value;
        this.ty2 = ty2;
    }

    public String getTy1() {
        return ty1;
    }

    public String getTy2() {
        return ty2;
    }

    public Value getValue() {
        return value;
    }

    public Value getResult() {
        return result;
    }

    public String irOut() {
        String instr = result.irOut() + " = zext " + ty1 + " " + value.irOut() + " to " + ty2;
        return instr;
    }
}

package midend.llvm.instr;

import midend.llvm.RetValue;

public class IrZext extends IrInstr{
    /* <result> = zext <ty> <value> to <ty2> */
    private final RetValue result;
    private final String ty1;
    private final RetValue value;
    private final String ty2;

    public IrZext(RetValue result, String ty1, RetValue value, String ty2) {
        this.result = result;
        this.ty1 = ty1;
        this.value = value;
        this.ty2 = ty2;
    }

    public String irOut() {
        String instr = result.irOut() + " = zext " + ty1 + " " + value.irOut() + " to " + ty2;
        return instr;
    }
}

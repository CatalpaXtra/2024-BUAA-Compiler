package midend.llvm.instr;

import midend.llvm.Value;

public class IrBr extends IrInstr {
    /* br label <dest> */
    /* br i1 <cond>, label <iftrue>, label <iffalse> */
    private String dest;
    private Value cond;
    private String ifTrue;
    private String ifFalse;

    public IrBr(String dest) {
        super(null, null);
        this.dest = dest;
    }

    public IrBr(Value cond, String ifTrue, String ifFalse) {
        super(null, null);
        this.dest = null;
        this.cond = cond;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
        addOperand(cond);
    }

    public Value getCond() {
        return cond;
    }

    public String getLabel() {
        return dest;
    }

    public String getLabel1() {
        return ifTrue;
    }

    public String getLabel2() {
        return ifFalse;
    }

    public void backFill(String replaced, String target) {
        if (dest != null) {
            dest = dest.equals(replaced) ? target : dest;
        } else {
            ifTrue = ifTrue.equals(replaced) ? target : ifTrue;
            ifFalse = ifFalse.equals(replaced) ? target : ifFalse;
        }
    }

    public String toString() {
        String instr;
        if (dest != null) {
            instr = "br label " + dest;
        } else {
            instr = "br i1 " + cond.getName() + ", label " + ifTrue + ", label " + ifFalse;
        }
        return instr;
    }

}

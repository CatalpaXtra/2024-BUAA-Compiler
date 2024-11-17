package midend.llvm.instr;

import midend.llvm.RetValue;

public class IrBr extends IrInstr {
    /* br label <dest> */
    /* br i1 <cond>, label <iftrue>, label <iffalse> */
    private String dest;
    private RetValue result;
    private String ifTrue;
    private String ifFalse;
    private boolean mode;

    public IrBr(String dest) {
        this.dest = dest;
        mode = true;
    }

    public IrBr(RetValue result, String ifTrue, String ifFalse) {
        this.result = result;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
        mode = false;
    }

    public void backFill(String replaced, String target) {
        if (mode) {
            dest = dest.equals(replaced) ? target : dest;
        } else {
            ifTrue = ifTrue.equals(replaced) ? target : ifTrue;
            ifFalse = ifFalse.equals(replaced) ? target : ifFalse;
        }
    }

    public String irOut() {
        String instr;
        if (mode) {
            instr = "br label " + dest;
        } else {
            instr = "br i1 " + result.irOut() + ", label " + ifTrue + ", label " + ifFalse;
        }
        return instr;
    }

}

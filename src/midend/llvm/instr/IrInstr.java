package midend.llvm.instr;

import midend.llvm.User;
import midend.llvm.function.IrBlock;

public class IrInstr extends User {
    private IrBlock parentBlock;

    public IrInstr(String name, String type) {
        super(name, type);
    }

    public IrInstr(String name, String type, IrBlock parentBlock) {
        super(name, type);
        this.parentBlock = parentBlock;
    }

    public boolean hasLVal(){
        return this instanceof IrAlloca ||
                (this instanceof IrCall && !this.getLlvmType().equals("void")) ||
                this instanceof IrGetelementptr ||
                this instanceof IrLoad ||
                this instanceof IrIcmp ||
                this instanceof IrTrunc ||
                this instanceof IrZext;
    }

    public IrBlock getParentBlock() {
        return parentBlock;
    }

    public void setParentBlock(IrBlock parentBlock) {
        this.parentBlock = parentBlock;
    }

    public String irOut() {
        return "";
    }
}

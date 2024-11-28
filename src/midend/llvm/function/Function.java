package midend.llvm.function;

import midend.llvm.Value;
import midend.llvm.symbol.Symbol;

import java.util.ArrayList;

public class Function extends Symbol {
    private final ArrayList<Param> params;
    private final IrBlock irBlock;

    public Function(String name, String irType, ArrayList<Param> params, IrBlock irBlock) {
        super(name, irType, new Value(null, null), -1, null);
        this.params = params;
        this.irBlock = irBlock;
    }

    public ArrayList<Param> getParams() {
        return params;
    }

    public IrBlock getIrBlock() {
        return irBlock;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        StringBuilder declParam = new StringBuilder();
        for (Param param : params) {
            declParam.append(param.irOut() + ", ");
        }
        if (!declParam.isEmpty()) {
            declParam.delete(declParam.length() - 2, declParam.length());
        }
        sb.append("define dso_local " + irType + " @" + name + "(" + declParam.toString() + ") {\n");
        sb.append(irBlock.toString());
        sb.append("}\n");
        return sb.toString();
    }
}

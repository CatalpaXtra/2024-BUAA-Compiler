package midend.llvm.function;

import frontend.parser.function.params.FuncFParam;
import midend.llvm.Module;
import midend.llvm.Support;
import midend.llvm.symbol.Symbol;

import java.util.ArrayList;

public class Function extends Symbol {
    private final ArrayList<Param> params;
    private final String name;
    private final String irType;
    private final IrBlock irBlock;

    public Function(String name, String symbolType, ArrayList<Param> params, IrBlock irBlock) {
        super(symbolType, name, "", -1);
        this.irType = Support.varTransfer(symbolType);
        this.name = name;
        this.params = params;
        this.irBlock = irBlock;
    }

    public String irOut() {
        StringBuilder sb = new StringBuilder();
        StringBuilder declParam = new StringBuilder();
        for (Param param : params) {
            declParam.append(param.irOut() + ", ");
        }
        if (!declParam.isEmpty()) {
            declParam.delete(declParam.length() - 2, declParam.length());
        }
        sb.append("define dso_local " + irType + " @" + name + "(" + declParam.toString() + ") {\n");
        sb.append(irBlock.irOut());
        sb.append("}\n");
        return sb.toString();
    }
}

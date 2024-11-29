package midend.llvm.instr;

import midend.llvm.Value;
import midend.llvm.function.Param;

import java.util.ArrayList;

public class IrCall extends IrInstr {
    /* <result> = call [ret attrs] <ty> <name>(<...args>) */
    private final String funcName;
    private final ArrayList<Param> params;
    private final ArrayList<Value> values;
    private final String rParams;

    public IrCall(String name, String funcType, String funcName, ArrayList<Param> params, ArrayList<Value> values) {
        super(name, funcType);
        this.funcName = funcName;
        this.params = params;
        this.values = values;
        this.rParams = getRParams();
    }

    public String getFuncName() {
        return funcName;
    }

    public String getRParams() {
        if (values.isEmpty()) {
            return "";
        }
        String passRParam = "";
        for (int i = 0; i < values.size(); i++) {
            passRParam += params.get(i).getIrType() + " " + values.get(i).getName() + ", ";
            addOperand(values.get(i));
        }
        return passRParam.substring(0, passRParam.length() - 2);
    }

    public String toString() {
        String instr;
        if (irType.equals("void")) {
            instr = "call void @" + funcName + "(" + rParams + ")";
        } else {
            instr = name + " = call " + irType + " @" + funcName + "(" + rParams + ")";
        }
        return instr;
    }
}

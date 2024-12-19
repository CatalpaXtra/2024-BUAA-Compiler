package midend.llvm.function;

import backend.Register;
import midend.llvm.Value;
import midend.llvm.symbol.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Function extends Symbol {
    private final ArrayList<Param> params;
    private final IrBlock irBlock;
    private HashMap<Value, Register> var2reg;
    private boolean hasSideEffects;
    private HashSet<Function> call;
    private ArrayList<Function> callList;

    public Function(String name, String irType, ArrayList<Param> params, IrBlock irBlock) {
        super(name, irType, new Value(null, null), -1, null);
        this.params = params;
        this.irBlock = irBlock;
        this.var2reg = new HashMap<>();
        this.call = new HashSet<>();
        this.callList = new ArrayList<>();
    }

    public void setCall(HashSet<Function> call) {
        this.call = call;
    }

    public HashSet<Function> getCall() {
        return call;
    }

    public void setCallList(ArrayList<Function> callList) {
        this.callList = callList;
    }

    public ArrayList<Function> getCallList() {
        return callList;
    }

    public void setSideEffects(boolean hasSideEffects) {
        this.hasSideEffects = hasSideEffects;
    }

    public boolean hasSideEffects() {
        return hasSideEffects;
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
            declParam.append(param.toString() + ", ");
        }
        if (!declParam.isEmpty()) {
            declParam.delete(declParam.length() - 2, declParam.length());
        }
        sb.append("define dso_local " + irType + " @" + name + "(" + declParam.toString() + ") {\n");
        sb.append(irBlock.toString());
        sb.append("}\n");
        return sb.toString();
    }

    public void setVar2reg(HashMap<Value, Register> var2reg) {
        this.var2reg = var2reg;
    }

    public HashMap<Value, Register> getVar2reg() {
        return var2reg;
    }
}

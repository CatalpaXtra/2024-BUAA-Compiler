package midend.llvm;

import midend.llvm.global.GlobalStr;
import midend.llvm.global.GlobalVal;

import java.util.ArrayList;

public class Module {
    private static ArrayList<GlobalVal> globalVals = new ArrayList<>();
    private static ArrayList<GlobalStr> globalStrs = new ArrayList<>();
    private final ArrayList<String> codeList;

    public Module() {
        this.codeList = new ArrayList<>();
    }

    public static void addGlobalVal(GlobalVal globalVal) {
        globalVals.add(globalVal);
    }

    public void addGlobalStr(GlobalStr globalStr) {
        globalStrs.add(globalStr);
    }

    public void addInstrAdd(RetValue result, RetValue op1, RetValue op2) {
        /* <result> = add <ty> <op1>, <op2> */
        String instr = result.irOut() + " = add i32 " + op1.irOut() + ", " + op2.irOut();
        codeList.add(instr);
    }

    public void addInstrSub(RetValue result, String op1, RetValue op2) {
        /* <result> = sub <ty> <op1>, <op2> */
        String instr = result.irOut() + " = sub i32 " + op1 + ", " + op2.irOut();
        codeList.add(instr);
    }

    public void addInstrMul(RetValue result, RetValue op1, RetValue op2) {
        /* <result> = mul <ty> <op1>, <op2> */
        String instr = result.irOut() + " = mul i32 " + op1.irOut() + ", " + op2.irOut();
        codeList.add(instr);
    }

    public void addInstrSdiv(RetValue result, RetValue op1, RetValue op2) {
        /* <result> = sdiv <ty> <op1>, <op2> */
        String instr = result.irOut() + " = sdiv i32 " + op1.irOut() + ", " + op2.irOut();
        codeList.add(instr);
    }

    public void addInstrSrem(RetValue result, RetValue op1, RetValue op2) {
        /* <result> = srem <type> <op1>, <op2> */
        String instr = result.irOut() + " = srem i32 " + op1.irOut() + ", " + op2.irOut();
        codeList.add(instr);
    }

    public void addInstrIcmp(RetValue result, String cond, RetValue op1, String op2) {
        /* <result> = icmp <cond> <ty> <op1>, <op2> */
        String instr = result.irOut() + " = icmp " + cond + " i32 " + op1.irOut() + ", " + op2;
        codeList.add(instr);
    }

    public void addInstrCall(RetValue result, String funcType, String funcName, String rParams) {
        /* <result> = call [ret attrs] <ty> <name>(<...args>) */
        String instr;
        if (funcType.equals("void")) {
            instr = "call void @" + funcName + "(" + rParams + ")";
        } else {
            instr = result.irOut() + " = call " + funcType + " @" + funcName + "(" + rParams + ")";
        }
        codeList.add(instr);
    }

    public void addInstrAllocaVar(RetValue result, String llvmType) {
        /* <result> = alloca <type> */
        String instr = result.irOut() + " = alloca " + llvmType;
        codeList.add(instr);
    }

    public void addInstrAllocaArray(RetValue result, int size, String llvmType) {
        /* <result> = alloca <type> */
        String instr = result.irOut() + " = alloca [" + size + " x " + llvmType + "]";
        codeList.add(instr);
    }

    public void addInstrLoad(RetValue result, String llvmType, String pointer) {
        /* <result> = load <ty>, ptr <pointer> */
        String instr = result.irOut() + " = load " + llvmType + ", " + llvmType + "* " + pointer;
        codeList.add(instr);
    }

    public void addInstrStore(String llvmType, String value, String pointer) {
        /* store <ty> <value>, ptr <pointer> */
        String instr = "store " + llvmType + " " + value + ", " + llvmType + "* " + pointer;
        codeList.add(instr);
    }

    public void addInstrGetelementptrArray(RetValue result, int size, String llvmType, String pointer, String offset) {
        /* %1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3 */
        String instr = result.irOut() + " = getelementptr inbounds [" + size + " x " + llvmType + "]";
        instr += ", [" + size + " x " + llvmType + "]* " + pointer + ", i32 0, i32 " + offset;
        codeList.add(instr);
    }

    public void addInstrGetelementptrPointer(RetValue result, String llvmType, String pointer, String offset) {
        /* %3 = getelementptr i32, i32* %2, i32 3 */
        String instr = result.irOut() + " = getelementptr inbounds " + llvmType + ", " + llvmType + "* " + pointer + ", i32 " + offset;
        codeList.add(instr);
    }

    public void addInstrBr(String dest) {
        /* br label <dest> */
        String instr = "br label " + dest;
        codeList.add(instr);
    }

    public void addInstrBrCond(RetValue result, String ifTrue, String ifFalse) {
        /* br i1 <cond>, label <iftrue>, label <iffalse> */
        String instr = "br i1 " + result.irOut() + ", label " + ifTrue + ", label " + ifFalse;
        codeList.add(instr);
    }

    public void addInstrRet(String retType, RetValue value) {
        /* ret <type> <value> , ret void */
        String instr;
        if (retType.equals("void")) {
            instr = "ret void";
        } else {
            instr = "ret " + retType + " " + value.irOut();
        }
        codeList.add(instr);
    }

    public void addInstrZext(RetValue result, String ty1, RetValue value, String ty2) {
        /* <result> = zext <ty> <value> to <ty2> */
        String instr = result.irOut() + " = zext " + ty1 + " " + value.irOut() + " to " + ty2;
        codeList.add(instr);
    }

    public void addInstrTrunc(RetValue result, String ty1, RetValue value, String ty2) {
        /* <result> = trunc <ty> <value> to <ty2> */
        String instr = result.irOut() + " = trunc " + ty1 + " " + value.irOut() + " to " + ty2;
        codeList.add(instr);
    }

    public void addRetIfNotExist() {
        String instr = codeList.get(codeList.size() - 1);
        if (!instr.contains("ret")) {
            codeList.add("ret void");
        }
    }

    public void addCode(String code) {
        codeList.add(code);
    }

    public void addCode(ArrayList<String> codes) {
        codeList.addAll(codes);
    }

    public void delLastCode() {
        codeList.remove(codeList.size() - 1);
    }

    public int getLoc() {
        return codeList.size() - 1;
    }

    public void replaceInterval(int left, int right, String replaced, String target) {
        if (left > right) {
            return;
        }
        for (int i = left; i <= right; i++) {
            String str = codeList.get(i);
            str = str.replace(target, replaced);
            codeList.set(i, str);
        }
    }

    public String irOut() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            declare i32 @getint()
            declare i32 @getchar()
            declare void @putint(i32)
            declare void @putch(i32)
            declare void @putstr(i8*)
            """);
        for (GlobalVal globalVal : globalVals) {
            sb.append(globalVal.irOut()).append('\n');
        }
        for (GlobalStr globalStr : globalStrs) {
            sb.append(globalStr.irOut()).append('\n');
        }

        for (String code : codeList) {
            sb.append(code).append('\n');
        }
        return sb.toString();
    }
}

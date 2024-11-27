package midend.llvm;

import midend.llvm.function.Function;
import midend.llvm.global.GlobalStr;
import midend.llvm.global.GlobalVal;

import java.util.ArrayList;

public class IrModule {
    private static final ArrayList<GlobalVal> globalVals = new ArrayList<>();
    private static final ArrayList<GlobalStr> globalStrs = new ArrayList<>();
    private static final ArrayList<Function> functions = new ArrayList<>();

    public static void addGlobalVal(GlobalVal globalVal) {
        globalVals.add(globalVal);
    }

    public static GlobalStr addGlobalStr(String string, int strLen) {
        for (GlobalStr globalStr : globalStrs) {
            if (globalStr.getString().equals(string)) {
                return globalStr;
            }
        }
        GlobalStr globalStr = new GlobalStr(string, strLen);
        globalStrs.add(globalStr);
        return globalStr;
    }

    public static void addFunc(Function function) {
        functions.add(function);
    }

    public static String irOut() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            declare i32 @getint()
            declare i32 @getchar()
            declare void @putint(i32)
            declare void @putch(i32)
            declare void @putstr(i8*)
            
            """);
        for (GlobalVal globalVal : globalVals) {
            sb.append(globalVal.toString()).append('\n');
        }
        sb.append('\n');
        for (GlobalStr globalStr : globalStrs) {
            sb.append(globalStr.toString()).append('\n');
        }
        sb.append('\n');
        for (Function function : functions) {
            sb.append(function.toString()).append('\n');
        }
        return sb.toString();
    }

    public static ArrayList<GlobalVal> getGlobalVals() {
        return globalVals;
    }

    public static ArrayList<GlobalStr> getGlobalStrs() {
        return globalStrs;
    }

    public static ArrayList<Function> getFunctions() {
        return functions;
    }
}

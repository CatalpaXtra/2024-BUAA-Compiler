package midend.llvm;

import midend.llvm.function.Function;
import midend.llvm.global.GlobalStr;
import midend.llvm.global.GlobalVal;

import java.util.ArrayList;

public class Module {
    private static ArrayList<GlobalVal> globalVals = new ArrayList<>();
    private static ArrayList<GlobalStr> globalStrs = new ArrayList<>();
    private static ArrayList<Function> functions = new ArrayList<>();

    public static void addGlobalVal(GlobalVal globalVal) {
        globalVals.add(globalVal);
    }

    public static void addGlobalStr(GlobalStr globalStr) {
        globalStrs.add(globalStr);
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
            sb.append(globalVal.irOut()).append('\n');
        }
        sb.append('\n');
        for (GlobalStr globalStr : globalStrs) {
            sb.append(globalStr.irOut()).append('\n');
        }
        sb.append('\n');
        for (Function function : functions) {
            sb.append(function.irOut()).append('\n');
        }
        return sb.toString();
    }
}

package midend.llvm;

import java.util.ArrayList;

public class Module {
    private final ArrayList<String> globalDecl;
    private static int strNum = 0;
    private final ArrayList<String> codeList;

    public Module() {
        this.globalDecl = new ArrayList<>();
        this.codeList = new ArrayList<>();
        globalDecl.add("""
            declare i32 @getint()
            declare i32 @getchar()
            declare void @putint(i32)
            declare void @putch(i8)
            declare void @putstr(i8*)
            """);
    }

    public void addGlobalVar(String code) {
        globalDecl.add(code);
    }

    public String addGlobalStr(int strLen, String string) {
        String strName;
        if (strNum == 0) {
            strName = "@.str";
            globalDecl.add("");
        } else {
            strName = "@.str." + strNum;
        }
        globalDecl.add(strName + " = private unnamed_addr constant [" + strLen + " x i8] c\"" + string + "\", align 1");
        strNum++;
        return strName;
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
        for (String code : globalDecl) {
            sb.append(code).append('\n');
        }
        for (String code : codeList) {
            sb.append(code).append('\n');
        }
        return sb.toString();
    }
}

package midend.llvm;

import java.util.ArrayList;

public class Module {
    private final ArrayList<String> codeList;

    public Module() {
        this.codeList = new ArrayList<>();
        codeList.add("declare i32 @getint()\n" +
                "declare i32 @getchar()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i8)\n" +
                "declare void @putstr(i8*)");
        codeList.add("");
    }

    public void addCode(String code) {
        codeList.add(code);
    }

    public void addCode(ArrayList<String> codes) {
        codeList.addAll(codes);
    }

    public String irOut() {
        StringBuilder sb = new StringBuilder();
        for (String code : codeList) {
            sb.append(code).append('\n');
        }
        return sb.toString();
    }
}

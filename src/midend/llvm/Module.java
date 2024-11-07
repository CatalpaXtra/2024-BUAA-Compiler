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
        for (String code : codeList) {
            sb.append(code).append('\n');
        }
        return sb.toString();
    }
}

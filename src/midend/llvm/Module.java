package midend.llvm;

import java.util.ArrayList;

public class Module {
    private final ArrayList<String> codeList;

    public Module() {
        this.codeList = new ArrayList<>();
    }

    public void addCode(String code) {
        codeList.add(code);
    }

    public String irOut() {
        StringBuilder sb = new StringBuilder();
        for (String code : codeList) {
            sb.append(code).append('\n');
        }
        return sb.toString();
    }
}

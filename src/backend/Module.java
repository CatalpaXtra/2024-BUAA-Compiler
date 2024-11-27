package backend;

import backend.global.AsmGlobal;

import java.util.ArrayList;

public class Module {
    private static final ArrayList<AsmGlobal> data = new ArrayList<>();
    private static final ArrayList<String> functions = new ArrayList<>();

    public static void addAsmGlobal(AsmGlobal asmGlobal) {
        data.add(asmGlobal);
    }

    public static String mipsOut() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (AsmGlobal asmGlobal : data) {
            sb.append("    ").append(asmGlobal.toString()).append('\n');
        }
        sb.append('\n');
        sb.append(".text\n");
        return sb.toString();
    }
}

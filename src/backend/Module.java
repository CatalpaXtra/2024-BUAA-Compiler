package backend;

import backend.global.AsmGlobal;
import backend.instr.*;

import java.util.ArrayList;

public class Module {
    private static final ArrayList<AsmGlobal> data = new ArrayList<>();
    private static final ArrayList<AsmInstr> text = new ArrayList<>();

    public static void addAsmGlobal(AsmGlobal asmGlobal) {
        data.add(asmGlobal);
    }

    public static void addAsmLabel(String name) {
        AsmLabel asmLabel = new AsmLabel(name);
        text.add(asmLabel);
    }

    public static void addAsmLi(Register reg, int value) {
        AsmLi asmLi = new AsmLi(reg, value);
        text.add(asmLi);
    }

    public static void addAsmLa(Register reg, String name) {
        AsmLa asmLa = new AsmLa(reg, name);
        text.add(asmLa);
    }

    public static void addAsmSyscall() {
        AsmSyscall asmSyscall = new AsmSyscall();
        text.add(asmSyscall);
    }

    public static String mipsOut() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (AsmGlobal asmGlobal : data) {
            sb.append("    ").append(asmGlobal.toString()).append('\n');
        }
        sb.append('\n');
        sb.append(".text\n");
        for (AsmInstr asmInstr : text) {
            if (asmInstr instanceof AsmLabel) {
                sb.append(asmInstr.toString()).append('\n');
            } else {
                sb.append("    ").append(asmInstr.toString()).append('\n');
            }
        }
        return sb.toString();
    }
}

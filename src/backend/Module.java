package backend;

import backend.global.AsmGlobal;
import backend.instr.*;
import midend.llvm.function.Param;

import java.util.ArrayList;

public class Module {
    private static final ArrayList<AsmGlobal> data = new ArrayList<>();
    private static final ArrayList<AsmInstr> text = new ArrayList<>();

    public static ArrayList<AsmInstr> getText() {
        return text;
    }

    public static boolean nearLabel(AsmJump instr) {
        int loc = text.size();
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).equals(instr)) {
                loc = i;
                break;
            }
        }
        for (int i = loc + 1; i < text.size(); i++) {
            if (text.get(i) instanceof AsmLabel) {
                if ((instr).getLabel().equals(((AsmLabel) text.get(i)).getLabel())) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public static AsmInstr getNextAsm(AsmInstr instr) {
        int loc = text.size();
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).equals(instr)) {
                loc = i;
                break;
            }
        }
        for (int i = loc + 1; i < text.size(); i++) {
            if (!(text.get(i) instanceof AsmNull)) {
                return text.get(i);
            }
        }
        return null;
    }

    public static void changeInstr(AsmInstr instr, Register to, Register from) {
        int loc = text.size();
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).equals(instr)) {
                loc = i;
                break;
            }
        }
        AsmMove asmMove = new AsmMove(to, from);
        text.set(loc, asmMove);
    }

    public static void addAsmGlobal(AsmGlobal asmGlobal) {
        data.add(asmGlobal);
    }

    public static void addAsmNull(String comment) {
        AsmNull asmNull;
        if (comment.isEmpty()) {
            asmNull = new AsmNull(comment);
        } else {
            asmNull = new AsmNull("#" + comment);
        }
        text.add(asmNull);
    }

    public static void addAsmAlu(AsmAlu.OP op, Register to, Register operand1, Register operand2, int num) {
        AsmAlu asmAlu = new AsmAlu(op, to, operand1, operand2, num);
        text.add(asmAlu);
    }

    public static void addAsmCmp(AsmCmp.OP op, Register to, Register operand1, Register operand2) {
        AsmCmp asmCmp = new AsmCmp(op, to, operand1, operand2);
        text.add(asmCmp);
    }

    public static void addAsmJump(AsmJump.OP op, Register to, String label) {
        AsmJump asmJump = new AsmJump(op, to, label);
        text.add(asmJump);
    }

    public static void addAsmBranch(AsmBranch.OP op, Register reg1, Register reg2, String label, int num) {
        AsmBranch asmBranch = new AsmBranch(op, reg1, reg2, label, num);
        text.add(asmBranch);
    }

    public static void addAsmMove(Register to, Register from) {
        AsmMove asmMove = new AsmMove(to, from);
        text.add(asmMove);
    }

    public static void addAsmMoveDiv(Register to, Register from) {
        AsmMoveDiv asmMoveDiv = new AsmMoveDiv(to, from);
        text.add(asmMoveDiv);
    }

    public static void addAsmMem(AsmMem.Type type, Register value, int offset, Register base) {
        AsmMem asmMem = new AsmMem(type, value, offset, base);
        text.add(asmMem);
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

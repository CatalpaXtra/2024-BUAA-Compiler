package midend.llvm;

import frontend.lexer.Token;

import java.util.ArrayList;

public class Support {
    public static String condTransfer(Token.Type type) {
        switch (type) {
            case GRE:
                return "sgt";
            case GEQ:
                return "sge";
            case LSS:
                return "slt";
            case LEQ:
                return "sle";
            case EQL:
                return "eq";
            case NEQ:
                return "ne";
            default:
                System.out.println("Cond Reach Unknown Branch");
                return "ERROR";
        }
    }

    public static String varTransfer(String type) {
        if (type.contains("Int")) {
            return "i32";
        } else if (type.contains("Char")) {
            return "i8";
        } else {
            return "varTransfer Error!!";
        }
    }

    public static String tokenTypeTransfer(Token.Type type) {
        if (type.equals(Token.Type.INTTK)) {
            return "i32";
        } else if (type.equals(Token.Type.CHARTK)) {
            return "i8";
        } else {
            return "void";
        }
    }

    public static ArrayList<String> splitPrintString(String string) {
        StringBuilder part = new StringBuilder();
        ArrayList<String> parts = new ArrayList<>();
        for (int i = 1; i < string.length() - 1; i++) {
            char current = string.charAt(i);
            if (current == '%' && i + 1 < string.length()) {
                char next = string.charAt(i + 1);
                if (next == 'd' || next == 'c') {
                    if (!part.isEmpty()) {
                        parts.add(part.toString() + "\\00");
                        part = new StringBuilder();
                    }
                    parts.add("%" + next);
                    i++;
                } else {
                    part.append(current);
                }
            } else if (current == '\\' && i + 1 < string.length()) {
                char next = string.charAt(i + 1);
                if (next == 'n') {
                    part.append("\\0A");
                    i++;
                } else if (next == '0') {
                    part.append("\\00");
                    i++;
                } else {
                    part.append(current);
                }
            } else {
                part.append(current);
            }
        }
        if (!part.isEmpty()) {
            parts.add(part.toString() + "\\00");
        }
        return parts;
    }
}

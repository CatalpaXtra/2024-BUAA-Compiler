package midend.llvm;

import frontend.lexer.Token;

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
}

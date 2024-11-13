package midend.llvm.decl;

import frontend.lexer.Token;
import frontend.parser.declaration.constDecl.constInitVal.ConstExpSet;
import frontend.parser.declaration.varDecl.initVal.ExpSet;
import frontend.parser.expression.ConstExp;
import frontend.parser.expression.Exp;
import frontend.parser.expression.add.AddExp;
import frontend.parser.expression.add.MulExp;
import frontend.parser.expression.primary.Character;
import frontend.parser.expression.primary.ExpInParent;
import frontend.parser.expression.primary.LVal;
import frontend.parser.expression.primary.PrimaryEle;
import frontend.parser.expression.primary.PrimaryExp;
import frontend.parser.expression.unary.UnaryEle;
import frontend.parser.expression.unary.UnaryExp;
import frontend.parser.expression.unary.UnaryOp;
import frontend.parser.expression.unary.UnaryOpExp;
import midend.llvm.symbol.Symbol;
import midend.llvm.symbol.SymbolCon;
import midend.llvm.symbol.SymbolTable;

import java.util.ArrayList;

public class ConstValue {
    public static ArrayList<Integer> visitConstExpSet(ConstExpSet constExpSet, SymbolTable symbolTable) {
        ArrayList<ConstExp> constExps = constExpSet.getConstExps();
        ArrayList<Integer> values = new ArrayList<>();
        for (ConstExp constExp : constExps) {
            int value = visitConstExp(constExp, symbolTable);
            values.add(value);
        }
        return values;
    }

    public static ArrayList<Integer> visitExpSet(ExpSet expSet, SymbolTable symbolTable) {
        ArrayList<Exp> exps = expSet.getExps();
        ArrayList<Integer> values = new ArrayList<>();
        for (Exp exp : exps) {
            int value = visitExp(exp, symbolTable);
            values.add(value);
        }
        return values;
    }

    public static int visitConstExp(ConstExp constExp, SymbolTable symbolTable) {
        return visitAddExp(constExp.getAddExp(), symbolTable);
    }

    public static int visitExp(Exp exp, SymbolTable symbolTable) {
        return visitAddExp(exp.getAddExp(), symbolTable);
    }

    private static int visitAddExp(AddExp addExp, SymbolTable symbolTable) {
        ArrayList<MulExp> mulExps = addExp.getLowerExps();
        int value = visitMulExp(mulExps.get(0), symbolTable);
        ArrayList<Token> operators = addExp.getOperators();
        for (int i = 1; i < mulExps.size(); i++) {
            if (operators.get(i - 1).getType().equals(Token.Type.PLUS)) {
                value += visitMulExp(mulExps.get(i), symbolTable);
            } else {
                value -= visitMulExp(mulExps.get(i), symbolTable);
            }
        }
        return value;
    }

    private static int visitMulExp(MulExp mulExp, SymbolTable symbolTable) {
        ArrayList<UnaryExp> unaryExps = mulExp.getLowerExps();
        int value  = visitUnaryExp(unaryExps.get(0), symbolTable);
        ArrayList<Token> operators = mulExp.getOperators();
        for (int i = 1; i < unaryExps.size(); i++) {
            if (operators.get(i - 1).getType().equals(Token.Type.MULT)) {
                value *= visitUnaryExp(unaryExps.get(i), symbolTable);
            } else if (operators.get(i - 1).getType().equals(Token.Type.DIV)) {
                value /= visitUnaryExp(unaryExps.get(i), symbolTable);
            } else {
                value %= visitUnaryExp(unaryExps.get(i), symbolTable);
            }
        }
        return value;
    }

    private static int visitUnaryExp(UnaryExp unaryExp, SymbolTable symbolTable) {
        UnaryEle unaryEle = unaryExp.getUnaryEle();
        // no exist call func
        if (unaryEle instanceof UnaryOpExp) {
            return visitUnaryOpExp((UnaryOpExp) unaryEle, symbolTable);
        } else if (unaryEle instanceof PrimaryExp) {
            return visitPrimaryExp((PrimaryExp) unaryEle, symbolTable);
        }
        return 0;
    }

    private static int visitUnaryOpExp(UnaryOpExp unaryOpExp, SymbolTable symbolTable) {
        UnaryOp unaryOp = unaryOpExp.getUnaryOp();
        UnaryExp unaryExp = unaryOpExp.getUnaryExp();
        if (unaryOp.getToken().getType().equals(Token.Type.PLUS)) {
            return visitUnaryExp(unaryExp, symbolTable);
        } else if (unaryOp.getToken().getType().equals(Token.Type.MINU)) {
            return - visitUnaryExp(unaryExp, symbolTable);
        }
        return 0;
    }

    private static int visitPrimaryExp(PrimaryExp primaryExp, SymbolTable symbolTable) {
        PrimaryEle primaryEle = primaryExp.getPrimaryEle();
        if (primaryEle instanceof ExpInParent) {
            return visitExp(((ExpInParent) primaryEle).getExp(), symbolTable);
        } else if (primaryEle instanceof LVal) {
            return visitLVal((LVal) primaryEle, symbolTable);
        } else if (primaryEle instanceof frontend.parser.expression.primary.Number) {
            return ((frontend.parser.expression.primary.Number) primaryEle).getIntConst().getVal();
        } else if (primaryEle instanceof frontend.parser.expression.primary.Character) {
            return ((Character) primaryEle).getCharConst().getVal();
        }
        return 0;
    }

    private static int visitLVal(LVal lVal, SymbolTable symbolTable) {
        int value = 0;
        Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getIdenfr());
        if (lVal.isArray()) {
            int loc = visitExp(lVal.getExp(), symbolTable);
            if (symbol instanceof SymbolCon) {
                value = ((SymbolCon) symbol).getValueAtLoc(loc);
            }
        } else {
            if (symbol instanceof SymbolCon) {
                value = ((SymbolCon) symbol).getValue();
            }
        }
        return value;
    }
}

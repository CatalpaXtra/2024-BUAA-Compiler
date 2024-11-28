package midend.llvm.global;

import frontend.lexer.Token;
import frontend.parser.declaration.Decl;
import frontend.parser.declaration.DeclEle;
import frontend.parser.declaration.constDecl.ConstDecl;
import frontend.parser.declaration.constDecl.ConstDef;
import frontend.parser.declaration.constDecl.constInitVal.ConstExpSet;
import frontend.parser.declaration.constDecl.constInitVal.ConstInitValEle;
import frontend.parser.declaration.varDecl.VarDecl;
import frontend.parser.declaration.varDecl.VarDef;
import frontend.parser.declaration.varDecl.initVal.ExpSet;
import frontend.parser.declaration.varDecl.initVal.InitValEle;
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
import frontend.parser.terminal.StringConst;
import midend.llvm.IrModule;
import midend.llvm.Support;
import midend.llvm.global.initval.IrArray;
import midend.llvm.global.initval.InitVal;
import midend.llvm.global.initval.IrString;
import midend.llvm.global.initval.IrVar;
import midend.llvm.symbol.*;

import java.util.ArrayList;

public class GlobalBuilder {
    public static void visitGlobalDecl(Decl decl, SymbolTable symbolTable) {
        DeclEle declEle = decl.getDeclEle();
        if (declEle instanceof ConstDecl) {
            GlobalBuilder.visitGlobalConstDecl((ConstDecl) declEle, symbolTable);
        } else {
            GlobalBuilder.visitGlobalVarDecl((VarDecl) declEle, symbolTable);
        }
    }

    private static void visitGlobalConstDecl(ConstDecl constDecl, SymbolTable symbolTable) {
        String type = constDecl.getBType().identifyType();
        ArrayList<ConstDef> constDefs = constDecl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            visitGlobalConstDef(constDef, type, symbolTable);
        }
    }

    private static void visitGlobalVarDecl(VarDecl varDecl, SymbolTable symbolTable) {
        String type = varDecl.getBType().identifyType();
        ArrayList<VarDef> varDefs = varDecl.getVarDefs();
        for (VarDef varDef : varDefs) {
            visitGlobalVarDef(varDef, type, symbolTable);
        }
    }

    private static void visitGlobalConstDef(ConstDef constDef, String type, SymbolTable symbolTable) {
        String irType = Support.varTransfer(type);
        String name = constDef.getIdent().getIdenfr();
        InitVal constant = null;
        int size = -1;
        if (constDef.isArray()) {
            size = visitConstExp(constDef.getConstExp(), symbolTable);

            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();
            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = visitConstExpSet((ConstExpSet) constInitValEle, symbolTable);
                constant = new IrArray(irType, initVal, size);
            } else if (constInitValEle instanceof StringConst) {
                String initVal = ((StringConst) constInitValEle).getToken().getContent();
                constant = new IrString(initVal, size);
            }
        } else {
            int initVal = visitConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            constant = new IrVar(initVal);
        }
        GlobalVal globalVal = new GlobalVal(name, irType, constant, size, true);
        IrModule.addGlobalVal(globalVal);
        symbolTable.addSymbol(globalVal);
    }

    private static void visitGlobalVarDef(VarDef varDef, String type, SymbolTable symbolTable) {
        String irType = Support.varTransfer(type);
        String name = varDef.getIdent().getIdenfr();
        InitVal constant = null;
        int size = -1;
        if (varDef.isArray()) {
            size = visitConstExp(varDef.getConstExp(), symbolTable);
            if (varDef.hasInitValue()) {
                InitValEle initValEle = varDef.getInitVal().getInitValEle();
                if (initValEle instanceof ExpSet) {
                    ArrayList<Integer> initVal = visitExpSet((ExpSet) initValEle, symbolTable);
                    constant = new IrArray(irType, initVal, size);
                } else if (initValEle instanceof StringConst) {
                    String initVal = ((StringConst) initValEle).getToken().getContent();
                    constant = new IrString(initVal, size);
                }
            }
        } else {
            int initVal = 0;
            if (varDef.hasInitValue()) {
                initVal = visitExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
            }
            constant = new IrVar(initVal);
        }
        GlobalVal globalVal = new GlobalVal(name, irType, constant, size, false);
        IrModule.addGlobalVal(globalVal);
        symbolTable.addSymbol(globalVal);
    }

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
        int value;
        Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getIdenfr());
        if (lVal.isArray()) {
            int loc = visitExp(lVal.getExp(), symbolTable);
            value = symbol.getValueAtLoc(loc);
        } else {
            value = symbol.getValue();
        }
        return value;
    }
}

package midend.llvm;

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
import midend.symbol.*;

import java.util.ArrayList;

public class GlobalDecl {
    private static Module module;

    public static void setGlobalDecl(Module md) {
        module = md;
    }

    public static void visitGlobalDecl(Decl decl, SymbolTable symbolTable) {
        DeclEle declEle = decl.getDeclEle();
        if (declEle instanceof ConstDecl) {
            GlobalDecl.visitGlobalConstDecl((ConstDecl) declEle, symbolTable);
        } else {
            GlobalDecl.visitGlobalVarDecl((VarDecl) declEle, symbolTable);
        }
    }

    public static void visitGlobalConstDecl(ConstDecl constDecl, SymbolTable symbolTable) {
        String type = constDecl.getBType().identifyType();
        ArrayList<ConstDef> constDefs = constDecl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            visitGlobalConstDef(constDef, type, symbolTable);
        }
    }

    public static void visitGlobalVarDecl(VarDecl varDecl, SymbolTable symbolTable) {
        String type = varDecl.getBType().identifyType();
        ArrayList<VarDef> varDefs = varDecl.getVarDefs();
        for (VarDef varDef : varDefs) {
            visitGlobalVarDef(varDef, type, symbolTable);
        }
    }

    private static String intArrayInit(int arraySize, ArrayList<Integer> initVal, String llvmType) {
        boolean zeroinitializer = true;
        String arrayFormat = "[";
        for (Integer val : initVal) {
            if (val != 0) {
                zeroinitializer = false;
            }
            arrayFormat += llvmType + " " + val + ", ";
        }
        if (zeroinitializer) {
            return "zeroinitializer";
        }
        for (int i = initVal.size(); i < arraySize; i++) {
            arrayFormat += llvmType + " 0, ";
        }
        return arrayFormat.substring(0, arrayFormat.length() - 2) + "]";
    }

    public static String charArrayInit(int arraySize, String initVal) {
        initVal = initVal.substring(1, initVal.length() - 1);
        String arrayFormat = "\"";
        int len = 0;
        for (int i = 0; i < initVal.length(); i++) {
            if (initVal.charAt(i) == '\\' && i + 1 < initVal.length()) {
                char next = initVal.charAt(i + 1);
                if (next == '0') {
                    arrayFormat += "\\00";
                } else if (next == 'n') {
                    arrayFormat += "\\0A";
                }
                i++;
            } else {
                arrayFormat += initVal.charAt(i);
            }
            len++;
        }
        for (int i = len; i < arraySize; i++) {
            arrayFormat += "\\00";
        }
        return arrayFormat + "\"";
    }

    private static void visitGlobalConstDef(ConstDef constDef, String type, SymbolTable symbolTable) {
        String symbolType = "Const" + type;
        String name = constDef.getIdent().getIdenfr();
        int line = constDef.getIdent().getLine();
        if (constDef.isArray()) {
            symbolType += "Array";
            int size = visitGlobalConstExp(constDef.getConstExp(), symbolTable);
            ConstInitValEle constInitValEle = constDef.getConstInitVal().getConstInitValEle();

            if (constInitValEle instanceof ConstExpSet) {
                ArrayList<Integer> initVal = visitGlobalConstExpSet((ConstExpSet) constInitValEle, symbolTable);
                String llvmType = Support.varTransfer(type);
                module.addGlobalVar("@" + name + " = dso_local global [" + size + " x " + llvmType + "] " + intArrayInit(size, initVal, llvmType));
                SymbolCon symbolCon = new SymbolCon(symbolType, name, line, "@" + name, initVal, size);
                symbolTable.addSymbol(symbolCon);
            } else if (constInitValEle instanceof StringConst) {
                String initVal = ((StringConst) constInitValEle).getToken().getContent();
                module.addGlobalVar("@" + name + " = dso_local global [" + size + " x i8] c" + charArrayInit(size, initVal) + ", align 1");
                SymbolCon symbolCon = new SymbolCon(symbolType, name, line, "@" + name, initVal, size);
                symbolTable.addSymbol(symbolCon);
            }
        } else {
            int initVal = visitGlobalConstExp((ConstExp) constDef.getConstInitVal().getConstInitValEle(), symbolTable);
            String llvmType = Support.varTransfer(type);
            module.addGlobalVar("@" + name + " = dso_local global " + llvmType + " " + initVal);
            SymbolCon symbolCon = new SymbolCon(symbolType, name, line, "@" + name, initVal);
            symbolTable.addSymbol(symbolCon);
        }
    }

    private static void visitGlobalVarDef(VarDef varDef, String type, SymbolTable symbolTable) {
        String symbolType = type;
        String name = varDef.getIdent().getIdenfr();
        int line = varDef.getIdent().getLine();
        if (varDef.isArray()) {
            symbolType += "Array";
            int size = visitGlobalConstExp(varDef.getConstExp(), symbolTable);
            if (varDef.hasInitValue()) {
                InitValEle initValEle = varDef.getInitVal().getInitValEle();
                if (initValEle instanceof ExpSet) {
                    ArrayList<Integer> initVal = visitGlobalExpSet((ExpSet) initValEle, symbolTable);
                    String llvmType = Support.varTransfer(type);
                    module.addGlobalVar("@" + name + " = dso_local global [" + size + " x " + llvmType + "] " + intArrayInit(size, initVal, llvmType));
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, line, "@" + name, initVal, size);
                    symbolTable.addSymbol(symbolVar);
                } else if (initValEle instanceof StringConst) {
                    String initVal = ((StringConst) initValEle).getToken().getContent();
                    module.addGlobalVar("@" + name + " = dso_local global [" + size + " x i8] c" + charArrayInit(size, initVal) + ", align 1");
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, line, "@" + name, initVal, size);
                    symbolTable.addSymbol(symbolVar);
                }
            } else {
                if (symbolType.contains("Int")) {
                    module.addGlobalVar("@" + name + " = dso_local global [" + size + " x i32] zeroinitializer");
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, line, "@" + name, new ArrayList<>(), size);
                    symbolTable.addSymbol(symbolVar);
                } else {
                    module.addGlobalVar("@" + name + " = dso_local global [" + size + " x i8] zeroinitializer, align 1");
                    SymbolVar symbolVar = new SymbolVar(symbolType, name, line, "@" + name, "", size);
                    symbolTable.addSymbol(symbolVar);
                }
            }
        } else {
            int initVal = 0;
            if (varDef.hasInitValue()) {
                initVal = visitGlobalExp((Exp) varDef.getInitVal().getInitValEle(), symbolTable);
            }
            String llvmType = Support.varTransfer(type);
            module.addGlobalVar("@" + name + " = dso_local global " + llvmType + " " + initVal);
            SymbolVar symbolVar = new SymbolVar(symbolType, name, line, "@" + name, initVal);
            symbolTable.addSymbol(symbolVar);
        }
    }

    public static ArrayList<Integer> visitGlobalConstExpSet(ConstExpSet constExpSet, SymbolTable symbolTable) {
        ArrayList<ConstExp> constExps = constExpSet.getConstExps();
        ArrayList<Integer> values = new ArrayList<>();
        for (ConstExp constExp : constExps) {
            int value = visitGlobalConstExp(constExp, symbolTable);
            values.add(value);
        }
        return values;
    }

    private static ArrayList<Integer> visitGlobalExpSet(ExpSet expSet, SymbolTable symbolTable) {
        ArrayList<Exp> exps = expSet.getExps();
        ArrayList<Integer> values = new ArrayList<>();
        for (Exp exp : exps) {
            int value = visitGlobalExp(exp, symbolTable);
            values.add(value);
        }
        return values;
    }

    public static int visitGlobalConstExp(ConstExp constExp, SymbolTable symbolTable) {
        return visitGlobalAddExp(constExp.getAddExp(), symbolTable);
    }

    private static int visitGlobalExp(Exp exp, SymbolTable symbolTable) {
        return visitGlobalAddExp(exp.getAddExp(), symbolTable);
    }

    private static int visitGlobalAddExp(AddExp addExp, SymbolTable symbolTable) {
        ArrayList<MulExp> mulExps = addExp.getLowerExps();
        int value = visitGlobalMulExp(mulExps.get(0), symbolTable);
        ArrayList<Token> operators = addExp.getOperators();
        for (int i = 1; i < mulExps.size(); i++) {
            if (operators.get(i - 1).getType().equals(Token.Type.PLUS)) {
                value += visitGlobalMulExp(mulExps.get(i), symbolTable);
            } else {
                value -= visitGlobalMulExp(mulExps.get(i), symbolTable);
            }
        }
        return value;
    }

    private static int visitGlobalMulExp(MulExp mulExp, SymbolTable symbolTable) {
        ArrayList<UnaryExp> unaryExps = mulExp.getLowerExps();
        int value  = visitGlobalUnaryExp(unaryExps.get(0), symbolTable);
        ArrayList<Token> operators = mulExp.getOperators();
        for (int i = 1; i < unaryExps.size(); i++) {
            if (operators.get(i - 1).getType().equals(Token.Type.MULT)) {
                value *= visitGlobalUnaryExp(unaryExps.get(i), symbolTable);
            } else if (operators.get(i - 1).getType().equals(Token.Type.DIV)) {
                value /= visitGlobalUnaryExp(unaryExps.get(i), symbolTable);
            } else {
                value %= visitGlobalUnaryExp(unaryExps.get(i), symbolTable);
            }
        }
        return value;
    }

    private static int visitGlobalUnaryExp(UnaryExp unaryExp, SymbolTable symbolTable) {
        UnaryEle unaryEle = unaryExp.getUnaryEle();
        // no exist call func
        if (unaryEle instanceof UnaryOpExp) {
            return visitGlobalUnaryOpExp((UnaryOpExp) unaryEle, symbolTable);
        } else if (unaryEle instanceof PrimaryExp) {
            return visitGlobalPrimaryExp((PrimaryExp) unaryEle, symbolTable);
        }
        return 0;
    }

    private static int visitGlobalUnaryOpExp(UnaryOpExp unaryOpExp, SymbolTable symbolTable) {
        UnaryOp unaryOp = unaryOpExp.getUnaryOp();
        UnaryExp unaryExp = unaryOpExp.getUnaryExp();
        if (unaryOp.getToken().getType().equals(Token.Type.PLUS)) {
            return visitGlobalUnaryExp(unaryExp, symbolTable);
        } else if (unaryOp.getToken().getType().equals(Token.Type.MINU)) {
            return - visitGlobalUnaryExp(unaryExp, symbolTable);
        }
        return 0;
    }

    private static int visitGlobalPrimaryExp(PrimaryExp primaryExp, SymbolTable symbolTable) {
        PrimaryEle primaryEle = primaryExp.getPrimaryEle();
        if (primaryEle instanceof ExpInParent) {
            return visitGlobalExp(((ExpInParent) primaryEle).getExp(), symbolTable);
        } else if (primaryEle instanceof LVal) {
            return visitGlobalLVal((LVal) primaryEle, symbolTable);
        } else if (primaryEle instanceof frontend.parser.expression.primary.Number) {
            return ((frontend.parser.expression.primary.Number) primaryEle).getIntConst().getVal();
        } else if (primaryEle instanceof frontend.parser.expression.primary.Character) {
            return ((Character) primaryEle).getCharConst().getVal();
        }
        return 0;
    }

    private static int visitGlobalLVal(LVal lVal, SymbolTable symbolTable) {
        int value = 0;
        Symbol symbol = symbolTable.getSymbol(lVal.getIdent().getIdenfr());
        if (lVal.isArray()) {
            int loc = visitGlobalExp(lVal.getExp(), symbolTable);
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

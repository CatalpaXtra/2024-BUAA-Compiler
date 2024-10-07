package frontend.parser;

import frontend.Error;
import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.declaration.Decl;
import frontend.parser.declaration.DeclParser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Parser {
    private final TokenIterator iterator;
    private final ArrayList<Error> errors;
    private final ArrayList<Decl> decls;
//    private final ArrayList<FuncDef> funcs;
//    private MainFuncDef mainFunc;

    public Parser(Lexer lexer) {
        this.iterator = new TokenIterator(lexer.getTokens());
        errors = new ArrayList<>();
        decls = new ArrayList<>();
//        funcs = new ArrayList<>();
//        mainFunc = null;
    }

//    public CompUnit parseCompUnit() {
//        parseDecls();
//        parseFuncDefs();
//        parseMainFuncDef();
//
//        CompUnit compUnit = new CompUnit(decls, funcDefs, mainFuncDef);
//        return compUnit;
//    }

    public void parseDecls() {
        Token first = iterator.getNextToken();
        Token second = iterator.getNextToken();
        while (iterator.hasNext()) {
            Token third = iterator.getNextToken();
            if (third.getType().equals(Token.Type.LPARENT)) {
                /* def function */
                iterator.traceBack(3);
                break;
            }
            if (first.getType().equals(Token.Type.CONSTTK) || first.getType().equals(Token.Type.CHARTK) ||
                    (first.getType().equals(Token.Type.INTTK) && second.getType().equals(Token.Type.IDENFR))) {
                /* first -> const/char || first -> int && second -> IDENFR */
                iterator.traceBack(3);
                DeclParser declParser = new DeclParser(iterator);
                decls.add(declParser.parseDecl());
            } else {
                iterator.traceBack(3);
                break;
            }
            first = iterator.getNextToken();
            second = iterator.getNextToken();
        }
    }

//    private void parseFuncDefs() {
//        Token first = iterator.getNextToken();
//        Token second = iterator.getNextToken();
//        while (iterator.hasNext()) {
//            Token third = iterator.getNextToken();
//            if (!third.getType().equals(Token.Type.LPARENT)) {
//                /* no def function */
//                iterator.traceBack(3);
//                break;
//            }
//            if ((first.getType().equals(Token.Type.INTTK) || first.getType().equals(Token.Type.CHARTK) || first.getType().equals(Token.Type.VOIDTK)) &&
//                    second.getType().equals(Token.Type.IDENFR)) {
//                /* first -> int/char/void && second -> IDENFR */
//                iterator.traceBack(3);
//                FuncDefParser funcDefParser = new FuncDefParser(iterator);
//                funcs.add(funcDefParser.parseFuncDef());
//            } else {
//                iterator.traceBack(3);
//                break;
//            }
//            first = iterator.getNextToken();
//            second = iterator.getNextToken();
//        }
//    }
//
//    private void parseMainFuncDef() {
//        MainFuncDefParser mainFuncDefParser = new MainFuncDefParser(iterator);
//        this.mainFunc = mainFuncDefParser.parseMainFuncDef();
//    }

    public void print() {
        StringBuilder output = new StringBuilder();
        for (Decl decl : decls) {
            output.append(decl.toString());
        }
        try (PrintWriter writer = new PrintWriter("lexer.txt")) {
            writer.println(output);
        } catch (FileNotFoundException ignored) {}
    }

}

package frontend.parser;

import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.declaration.Decl;
import frontend.parser.declaration.DeclParser;
import frontend.parser.function.FuncDef;
import frontend.parser.function.FuncDefParser;
import frontend.parser.function.MainFuncDef;
import frontend.parser.function.MainFuncDefParser;

import java.util.ArrayList;

public class Parser {
    private final TokenIterator iterator;
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private MainFuncDef mainFuncDef;

    public Parser(Lexer lexer) {
        this.iterator = new TokenIterator(lexer.getTokens());
        this.decls = new ArrayList<>();
        this.funcDefs = new ArrayList<>();
    }

    public CompUnit parse() {
        parseDecls();
        parseFuncDefs();
        parseMainFuncDef();
        CompUnit compUnit = new CompUnit(decls, funcDefs, mainFuncDef);
        return compUnit;
    }

    private void parseDecls() {
        while (iterator.hasNext()) {
            Token first = iterator.getNextToken();
            Token second = iterator.getNextToken();
            Token third = iterator.getNextToken();
            iterator.traceBack(3);
            if (third.getType().equals(Token.Type.LPARENT)) {
                /* def function */
                break;
            }
            DeclParser declParser = new DeclParser(iterator);
            decls.add(declParser.parseDecl());
        }
    }

    private void parseFuncDefs() {
        while (iterator.hasNext()) {
            Token first = iterator.getNextToken();
            Token second = iterator.getNextToken();
            iterator.traceBack(2);
            if (second.getType().equals(Token.Type.MAINTK)) {
                /* def main function */
                break;
            }
            FuncDefParser funcDefParser = new FuncDefParser(iterator);
            funcDefs.add(funcDefParser.parseFuncDef());
        }
    }

    private void parseMainFuncDef() {
        MainFuncDefParser mainFuncDefParser = new MainFuncDefParser(iterator);
        mainFuncDef = mainFuncDefParser.parseMainFuncDef();
    }

}

package frontend.parser.function.params;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;

import java.util.ArrayList;

public class FuncFParamsParser {
    private final TokenIterator iterator;
    private final ArrayList<FuncFParam> funcFParamList;
    private final ArrayList<Token> commas;

    public FuncFParamsParser(TokenIterator iterator) {
        this.iterator = iterator;
        this.funcFParamList = new ArrayList<>();
        this.commas = new ArrayList<>();
    }

    public FuncFParams parseFuncFParams() {
        FuncFParamParser funcFParamParser = new FuncFParamParser(iterator);
        funcFParamList.add(funcFParamParser.parseFuncFParam());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.COMMA)) {
            commas.add(token);
            funcFParamList.add(funcFParamParser.parseFuncFParam());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        FuncFParams funcFParams = new FuncFParams(funcFParamList, commas);
        return funcFParams;
    }
}

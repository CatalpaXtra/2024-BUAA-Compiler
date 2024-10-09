package frontend.parser.function;

import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.declaration.BType;
import frontend.parser.declaration.BTypeParser;
import frontend.parser.terminal.Ident;
import frontend.parser.terminal.IdentParser;

public class FuncFParamParser {
    private final TokenIterator iterator;
    private BType bType;
    private Ident ident;
    private Token lBracket;
    private Token rBracket;

    public FuncFParamParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public FuncFParam parseFuncFParam() {
        BTypeParser bTypeParser = new BTypeParser(iterator);
        bType = bTypeParser.parseBtype();
        IdentParser identParser = new IdentParser(iterator);
        ident = identParser.parseIdent();
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.LBRACK)) {
            lBracket = token;
            ErrorHandler errorHandler = new ErrorHandler(iterator);
            rBracket = errorHandler.handleErrorK();
        } else {
            iterator.traceBack(1);
            lBracket = rBracket = null;
        }
        FuncFParam funcFParam = new FuncFParam(bType, ident, lBracket, rBracket);
        return funcFParam;
    }
}

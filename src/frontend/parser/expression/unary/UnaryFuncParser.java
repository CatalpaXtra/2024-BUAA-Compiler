package frontend.parser.expression.unary;

import frontend.Error;
import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.ParserErrors;
import frontend.parser.terminal.Ident;
import frontend.parser.terminal.IdentParser;

public class UnaryFuncParser {
    private final TokenIterator iterator;
    private Ident ident;
    private Token lParent;
    private FuncRParams funcRParams;
    private Token rParent;

    public UnaryFuncParser(TokenIterator iterator) {
        this.iterator = iterator;
        this.funcRParams = null;
    }

    public UnaryFunc parseUnaryFunc() {
        IdentParser identParser = new IdentParser(iterator);
        ident = identParser.parseIdent();
        lParent = iterator.getNextToken();
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.RPARENT)) {
            funcRParams = null;
            rParent = token;
        } else if (isExp(token)) {
            iterator.traceBack(1);
            FuncRParamsParser funcRParamsParser = new FuncRParamsParser(iterator);
            funcRParams = funcRParamsParser.parseFuncRParams();
            ErrorHandler errorHandler = new ErrorHandler(iterator);
            rParent = errorHandler.handleErrorJ();
        } else {
            funcRParams = null;
            iterator.traceBack(2);
            token = iterator.getNextToken();
            Error error = new Error(Error.Type.j, ")", token.getLine());
            ParserErrors.addError(error);
            rParent = null;
        }
        UnaryFunc unaryFunc = new UnaryFunc(ident, lParent, funcRParams, rParent);
        return unaryFunc;
    }

    private boolean isExp(Token token) {
        return token.getType().equals(Token.Type.LPARENT) ||
                token.getType().equals(Token.Type.IDENFR) ||
                token.getType().equals(Token.Type.INTCON) ||
                token.getType().equals(Token.Type.CHRCON) ||
                token.getType().equals(Token.Type.PLUS) ||
                token.getType().equals(Token.Type.MINU) ||
                token.getType().equals(Token.Type.NOT);
    }
}

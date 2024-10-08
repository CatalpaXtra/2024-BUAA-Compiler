package frontend.parser.expression.unary;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.primary.PrimaryExpParser;

public class UnaryExpParser {
    private final TokenIterator iterator;
    private UnaryEle unaryEle;

    public UnaryExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public UnaryExp parseUnaryExp() {
        Token first = iterator.getNextToken();
        Token second = iterator.getNextToken();
        iterator.traceBack(2);
        if (isIdent(first, second)) {
            UnaryFuncParser unaryFuncParser = new UnaryFuncParser(iterator);
            unaryEle = unaryFuncParser.parseUnaryFunc();
        } else if (isUnaryOp(first)) {
            UnaryOpExpParser unaryOpExpParser = new UnaryOpExpParser(iterator);
            unaryEle = unaryOpExpParser.parseUnaryOpExp();
        } else if (isPrimaryExp(first)) {
            PrimaryExpParser primaryExpParser = new PrimaryExpParser(iterator);
            unaryEle = primaryExpParser.parsePrimaryExp();
        }
        UnaryExp unaryExp = new UnaryExp(unaryEle);
        return unaryExp;
    }

    private boolean isIdent(Token first, Token second) {
        return first.getType().equals(Token.Type.IDENFR) &&
                second.getType().equals(Token.Type.LPARENT);
    }

    private boolean isUnaryOp(Token first) {
        return first.getType().equals(Token.Type.PLUS) ||
                first.getType().equals(Token.Type.MINU) ||
                first.getType().equals(Token.Type.NOT);
    }

    private boolean isPrimaryExp(Token first) {
        return first.getType().equals(Token.Type.LPARENT) ||
                first.getType().equals(Token.Type.IDENFR) ||
                first.getType().equals(Token.Type.INTCON) ||
                first.getType().equals(Token.Type.CHRCON);
    }
}

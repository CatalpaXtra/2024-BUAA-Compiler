package frontend.parser.expression.unary;

import frontend.lexer.TokenIterator;

public class UnaryOpExpParser {
    private final TokenIterator iterator;
    private UnaryOp unaryOp;
    private UnaryExp unaryExp;

    public UnaryOpExpParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public UnaryOpExp parseUnaryOpExp() {
        UnaryOpParser unaryOpParser = new UnaryOpParser(iterator);
        unaryOp = unaryOpParser.parseUnaryOp();
        UnaryExpParser unaryExpParser = new UnaryExpParser(iterator);
        unaryExp = unaryExpParser.parseUnaryExp();
        UnaryOpExp unaryOpExp = new UnaryOpExp(unaryOp, unaryExp);
        return unaryOpExp;
    }
}

package frontend.parser.declaration.varDecl.initVal;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.ExpParser;
import frontend.parser.terminal.StringConstParser;

public class InitValParser {
    private final TokenIterator iterator;
    private InitValEle initValEle;

    public InitValParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public InitVal parseInitVal() {
        Token token = iterator.getNextToken();
        iterator.traceBack(1);
        if (token.getType().equals(Token.Type.LBRACE)) {
            ExpSetParser expSetParser = new ExpSetParser(iterator);
            initValEle = expSetParser.parseExpSet();
        } else if (token.getType().equals(Token.Type.STRCON)) {
            StringConstParser stringConstParser = new StringConstParser(iterator);
            initValEle = stringConstParser.parseStringConst();
        } else {
            ExpParser expParser = new ExpParser(iterator);
            initValEle = expParser.parseExp();
        }
        InitVal initVal = new InitVal(initValEle);
        return initVal;
    }
}

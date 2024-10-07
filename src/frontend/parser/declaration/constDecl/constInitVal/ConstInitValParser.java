package frontend.parser.declaration.constDecl.constInitVal;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.ConstExpParser;
import frontend.parser.terminal.StringConstParser;

public class ConstInitValParser {
    private final TokenIterator iterator;
    private ConstInitValEle constInitValEle;

    public ConstInitValParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public ConstInitVal parseConstInitVal() {
        Token token = iterator.getNextToken();
        iterator.traceBack(1);
        if (token.getType().equals(Token.Type.LBRACE)) {
            ConstExpSetParser constExpSetParser = new ConstExpSetParser(iterator);
            constInitValEle = constExpSetParser.parseConstExpSet();
        } else if (token.getType().equals(Token.Type.STRCON)) {
            StringConstParser stringConstParser = new StringConstParser(iterator);
            constInitValEle = stringConstParser.parseStringConst();
        } else {
            ConstExpParser constExpParser = new ConstExpParser(iterator);
            constInitValEle = constExpParser.parseConstExp();
        }
        ConstInitVal constInitVal = new ConstInitVal(constInitValEle);
        return constInitVal;
    }
}

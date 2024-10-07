package frontend.parser.declaration.constDecl;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.declaration.constDecl.constInitVal.ConstInitVal;
import frontend.parser.declaration.constDecl.constInitVal.ConstInitValParser;
import frontend.parser.terminal.Ident;
import frontend.parser.terminal.IdentParser;
import frontend.parser.expression.ConstExp;
import frontend.parser.expression.ConstExpParser;

public class ConstDefParser {
    private final TokenIterator iterator;
    private Ident ident;
    private Token lBracket;
    private ConstExp constExp;
    private Token rBracket;
    private Token eq;
    private ConstInitVal constInitVal;

    public ConstDefParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public ConstDef parseConstDef() {
        IdentParser identParser = new IdentParser(iterator);
        ident = identParser.parseIdent();
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.LBRACK)) {
            lBracket = token;
            ConstExpParser constExpParser = new ConstExpParser(iterator);
            constExp = constExpParser.parseConstExp();
            rBracket = iterator.getNextToken();
        } else {
            iterator.traceBack(1);
            lBracket = rBracket = null;
            constExp = null;
        }
        eq = iterator.getNextToken();
        ConstInitValParser constInitValParser = new ConstInitValParser(iterator);
        constInitVal = constInitValParser.parseConstInitVal();
        ConstDef constDef = new ConstDef(ident, lBracket, constExp, rBracket, eq, constInitVal);
        return constDef;
    }
}

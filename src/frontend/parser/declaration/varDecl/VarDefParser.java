package frontend.parser.declaration.varDecl;

import frontend.parser.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.declaration.varDecl.initVal.InitVal;
import frontend.parser.declaration.varDecl.initVal.InitValParser;
import frontend.parser.expression.ConstExp;
import frontend.parser.expression.ConstExpParser;
import frontend.parser.terminal.Ident;
import frontend.parser.terminal.IdentParser;

public class VarDefParser {
    private final TokenIterator iterator;
    private Ident ident;
    private Token lBracket;
    private ConstExp constExp;
    private Token rBracket;
    private Token eq;
    private InitVal initVal;

    public VarDefParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public VarDef parseVarDef() {
        IdentParser identParser = new IdentParser(iterator);
        ident = identParser.parseIdent();
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.LBRACK)) {
            lBracket = token;
            ConstExpParser constExpParser = new ConstExpParser(iterator);
            constExp = constExpParser.parseConstExp();
            ErrorHandler errorHandler = new ErrorHandler(iterator);
            rBracket = errorHandler.handleErrorK();
        } else {
            iterator.traceBack(1);
            lBracket = rBracket = null;
            constExp = null;
        }

        token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.ASSIGN)) {
            eq = token;
            InitValParser initValParser = new InitValParser(iterator);
            initVal = initValParser.parseInitVal();
        } else {
            iterator.traceBack(1);
            eq = null;
            initVal = null;
        }
        VarDef varDef = new VarDef(ident, lBracket, constExp, rBracket, eq, initVal);
        return varDef;
    }
}

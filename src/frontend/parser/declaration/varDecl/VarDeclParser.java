package frontend.parser.declaration.varDecl;

import frontend.parser.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.declaration.BType;
import frontend.parser.declaration.BTypeParser;
import java.util.ArrayList;

public class VarDeclParser {
    private final TokenIterator iterator;
    private BType btype;
    private final ArrayList<VarDef> varDefs;
    private final ArrayList<Token> commas;
    private Token semicolon;

    public VarDeclParser(TokenIterator iterator) {
        this.iterator = iterator;
        this.varDefs = new ArrayList<>();
        this.commas = new ArrayList<>();
    }

    public VarDecl parseVarDecl() {
        BTypeParser btypeParser = new BTypeParser(iterator);
        btype = btypeParser.parseBtype();
        VarDefParser varDefParser = new VarDefParser(iterator);
        varDefs.add(varDefParser.parseVarDef());
        Token token = iterator.getNextToken();
        while (token.getType().equals(Token.Type.COMMA)) {
            commas.add(token);
            varDefs.add(varDefParser.parseVarDef());
            token = iterator.getNextToken();
        }
        iterator.traceBack(1);
        ErrorHandler errorHandler = new ErrorHandler(iterator);
        semicolon = errorHandler.handleErrorI();
        VarDecl varDecl = new VarDecl(btype, varDefs, commas, semicolon);
        return varDecl;
    }
}

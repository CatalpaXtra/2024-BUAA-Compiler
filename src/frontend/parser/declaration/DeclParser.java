package frontend.parser.declaration;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.declaration.constDecl.ConstDeclParser;
import frontend.parser.declaration.varDecl.VarDeclParser;

public class DeclParser {
    private final TokenIterator iterator;
    private DeclEle declEle;

    public DeclParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Decl parseDecl() {
        Token first = iterator.getNextToken();
        iterator.traceBack(1);
        if (first.getType().equals(Token.Type.CONSTTK)) {
            ConstDeclParser constDeclParser = new ConstDeclParser(iterator);
            declEle = constDeclParser.parseConstDecl();
        } else if (first.getType().equals(Token.Type.INTTK) || first.getType().equals(Token.Type.CHARTK)) {
            VarDeclParser varDeclParser = new VarDeclParser(iterator);
            declEle = varDeclParser.parseVarDecl();
        }
        Decl decl = new Decl(declEle);
        return decl;
    }
}

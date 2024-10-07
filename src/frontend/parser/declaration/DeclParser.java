package frontend.parser.declaration;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.declaration.constDecl.ConstDeclParser;

public class DeclParser {
    private final TokenIterator iterator;

    public DeclParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public Decl parseDecl() {
        Token first = iterator.getNextToken();
        iterator.traceBack(1);
        DeclEle declEle = null;
        if (first.getType().equals(Token.Type.CONSTTK)) {
            ConstDeclParser constDeclParser = new ConstDeclParser(iterator);
            declEle = constDeclParser.parseConstDecl();
//        } else if (first.getType().equals(Token.Type.INTTK) || first.getType().equals(Token.Type.CHRCON)) {
//            VarDeclParser varDeclParser = new VarDeclParser(iterator);
//            declEle = varDeclParser.parseVarDecl();
        } else {
            /* ERROR */
            System.out.println("READ UNEXPECTED TOKEN ");
        }
        Decl decl = new Decl(declEle);
        return decl;
    }
}

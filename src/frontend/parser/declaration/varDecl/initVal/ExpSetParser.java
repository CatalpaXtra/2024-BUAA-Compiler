package frontend.parser.declaration.varDecl.initVal;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.expression.ConstExpParser;
import frontend.parser.expression.Exp;
import frontend.parser.expression.ExpParser;

import java.util.ArrayList;

public class ExpSetParser {
    private final TokenIterator iterator;
    private Token lBrace;
    private final ArrayList<Exp> exps;
    private final ArrayList<Token> commas;
    private Token rBrace;

    public ExpSetParser(TokenIterator iterator) {
        this.iterator = iterator;
        this.exps = new ArrayList<>();
        this.commas = new ArrayList<>();
    }

    public ExpSet parseExpSet() {
        lBrace = iterator.getNextToken();
        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.RBRACE)) {
            rBrace = token;
        } else {
            iterator.traceBack(1);
            ExpParser expParser = new ExpParser(iterator);
            exps.add(expParser.parseExp());
            token = iterator.getNextToken();
            while (token.getType().equals(Token.Type.COMMA)) {
                commas.add(token);
                exps.add(expParser.parseExp());
                token = iterator.getNextToken();
            }
            rBrace = token;
        }
        ExpSet expSet = new ExpSet(lBrace, exps, commas, rBrace);
        return expSet;
    }
}

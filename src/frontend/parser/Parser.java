package frontend.parser;

import frontend.Error;
import frontend.lexer.Token;

import java.util.ArrayList;

public class Parser {
    private final ArrayList<Token> tokens;
    private final ArrayList<Error> errors;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        errors = new ArrayList<>();
    }

}

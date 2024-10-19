package frontend.lexer;

import java.util.ArrayList;

public class LexerErrors {
    private static final ArrayList<Error> errors = new ArrayList<>();

    public static void addError(Error error) {
        errors.add(error);
    }

    public static ArrayList<Error> getErrors() {
        return errors;
    }
}

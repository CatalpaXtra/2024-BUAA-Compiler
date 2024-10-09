package frontend;

import java.util.ArrayList;

public class CompErrors {
    private final ArrayList<Error> lexerErrors;
    private final ArrayList<Error> parserErrors;

    public CompErrors(ArrayList<Error> lexerErrors, ArrayList<Error> parserErrors) {
        this.lexerErrors = lexerErrors;
        this.parserErrors = parserErrors;
    }

    public boolean existError() {
        return !(lexerErrors.isEmpty() && parserErrors.isEmpty());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int len1 = lexerErrors.size();
        int len2 = parserErrors.size();
        int i = 0, j = 0;
        while (i < len1 && j < len2) {
            if (lexerErrors.get(i).getLine() <= parserErrors.get(j).getLine()) {
                sb.append(lexerErrors.get(i++).toString());
            } else {
                sb.append(parserErrors.get(j++).toString());
            }
        }
        while (i < len1) {
            sb.append(lexerErrors.get(i++).toString());
        }
        while (j < len2) {
            sb.append(parserErrors.get(j++).toString());
        }
        return sb.toString();
    }
}

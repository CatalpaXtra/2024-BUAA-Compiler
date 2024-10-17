package frontend;

import java.util.ArrayList;

public class CompErrors {
    private final ArrayList<Error> lexerErrors;
    private final ArrayList<Error> parserErrors;
    private final ArrayList<Error> semanticErrors;
    private final ArrayList<Error> globalErrors;

    public CompErrors(ArrayList<Error> lexerErrors, ArrayList<Error> parserErrors, ArrayList<Error> semanticErrors) {
        this.lexerErrors = lexerErrors;
        this.parserErrors = parserErrors;
        this.semanticErrors = semanticErrors;

        this.globalErrors = mergeList(mergeList(lexerErrors, parserErrors), semanticErrors);
    }

    private ArrayList<Error> mergeList(ArrayList<Error> errors1, ArrayList<Error> errors2) {
        ArrayList<Error> errors = new ArrayList<>();
        int len1 = errors1.size();
        int len2 = errors2.size();
        int i = 0, j = 0;
        while (i < len1 && j < len2) {
            if (errors1.get(i).getLine() <= errors2.get(j).getLine()) {
                errors.add(errors1.get(i++));
            } else {
                errors.add(errors2.get(j++));
            }
        }
        while (i < len1) {
            errors.add(errors1.get(i++));
        }
        while (j < len2) {
            errors.add(errors2.get(j++));
        }
        return errors;
    }

    public boolean existError() {
        return !(lexerErrors.isEmpty() && parserErrors.isEmpty() && semanticErrors.isEmpty());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Error error : globalErrors) {
            sb.append(error.toString());
        }
        return sb.toString();
    }
}

package frontend;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

public class Lexer {

    private final ArrayList<Token> tokens;
    private final ArrayList<Token> errors;
    private final HashSet<String> identifiers;
    private boolean annotate;
    private boolean declareVar;

    public Lexer() {
        tokens = new ArrayList<>();
        errors = new ArrayList<>();
        identifiers = new HashSet<>();
        annotate = false;
        declareVar = false;
    }

    public void lexer(String input, int line) {
        int loc = 0;
        while (loc < input.length()) {
            if (annotate) {
                if (input.charAt(loc) == '*' && loc + 1 < input.length() && input.charAt(loc + 1) == '/') {
                    annotate = false;
                    loc += 2;
                    continue;
                } else {
                    loc++;
                    continue;
                }
            }
            StringBuilder content = new StringBuilder();
            if (input.charAt(loc) == ' ' || input.charAt(loc) == '\t') {
                loc++;
                continue;
            } else if (input.charAt(loc) == '/' && loc + 1 < input.length() && input.charAt(loc + 1) == '/') {
                break;
            } else if (input.charAt(loc) == '/' && loc + 1 < input.length() && input.charAt(loc + 1) == '*') {
                annotate = true;
                continue;
            }

            if (input.charAt(loc) == '_' ||
                    (input.charAt(loc) >= 'A' && input.charAt(loc) <= 'Z') ||
                    (input.charAt(loc) >= 'a' && input.charAt(loc) <= 'z')) {
                content.append(input.charAt(loc));
                loc++;
                while (loc < input.length() && (input.charAt(loc) == '_' ||
                        (input.charAt(loc) >= 'A' && input.charAt(loc) <= 'Z') ||
                        (input.charAt(loc) >= 'a' && input.charAt(loc) <= 'z') ||
                        (input.charAt(loc) >= '0' && input.charAt(loc) <= '9'))) {
                    content.append(input.charAt(loc));
                    loc++;
                }
                if (!matchWord(content.toString())) {
                    if (declareVar) {
                        identifiers.add(content.toString());
                        tokens.add(new Token(Token.Type.IDENFR, content.toString()));
                    } else if (identifiers.contains(content.toString())) {
                        tokens.add(new Token(Token.Type.IDENFR, content.toString()));
                    }
                }
                loc--;
            } else if (input.charAt(loc) >= '0' && input.charAt(loc) <= '9') {
                content.append(input.charAt(loc));
                loc++;
                while (loc < input.length() &&
                        (input.charAt(loc) >= '0' && input.charAt(loc) <= '9')) {
                    content.append(input.charAt(loc));
                    loc++;
                }
                tokens.add(new Token(Token.Type.INTCON, content.toString()));
                loc--;
            } else if (input.charAt(loc) == '\'') {
                content.append(input.charAt(loc));
                loc++;
                if (input.charAt(loc) == '\\') {
                    content.append(input.charAt(loc++)).append(input.charAt(loc));
                } else {
                    content.append(input.charAt(loc));
                }
                content.append(input.charAt(++loc));
                tokens.add(new Token(Token.Type.CHRCON, content.toString()));
            } else if (input.charAt(loc) == '\"') {
                content.append(input.charAt(loc));
                loc++;
                while (input.charAt(loc) != '\"') {
                    if (input.charAt(loc) == '\\') {
                        content.append(input.charAt(loc++)).append(input.charAt(loc));
                    } else {
                        content.append(input.charAt(loc));
                    }
                    loc++;
                }
                content.append(input.charAt(loc));
                tokens.add(new Token(Token.Type.STRCON, content.toString()));
            } else {
                switch (input.charAt(loc)) {
                    case '&':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '&') {
                            tokens.add(new Token(Token.Type.AND, "&&"));
                        } else {
                            errors.add(new Token(Token.Type.ERRA, line + ""));
                            loc--;
                        }
                        break;
                    case '|':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '|') {
                            tokens.add(new Token(Token.Type.OR, "||"));
                        } else {
                            errors.add(new Token(Token.Type.ERRA, line + ""));
                            loc--;
                        }
                        break;
                    case '!':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '=') {
                            tokens.add(new Token(Token.Type.NEQ, "!="));
                        } else {
                            tokens.add(new Token(Token.Type.NOT, "!"));
                            loc--;
                        }
                        break;
                    case '<':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '=') {
                            tokens.add(new Token(Token.Type.LEQ, "<="));
                        } else {
                            tokens.add(new Token(Token.Type.LSS, "<"));
                            loc--;
                        }
                        break;
                    case '>':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '=') {
                            tokens.add(new Token(Token.Type.GEQ, ">="));
                        } else {
                            tokens.add(new Token(Token.Type.GRE, ">"));
                            loc--;
                        }
                        break;
                    case '=':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '=') {
                            tokens.add(new Token(Token.Type.EQL, "=="));
                        } else {
                            tokens.add(new Token(Token.Type.ASSIGN, "="));
                            loc--;
                        }
                        break;
                    case '+':
                        tokens.add(new Token(Token.Type.PLUS, "+"));
                        break;
                    case '-':
                        tokens.add(new Token(Token.Type.MINU, "-"));
                        break;
                    case '*':
                        tokens.add(new Token(Token.Type.MULT, "*"));
                        break;
                    case '/':
                        tokens.add(new Token(Token.Type.DIV, "/"));
                        break;
                    case '%':
                        tokens.add(new Token(Token.Type.MOD, "%"));
                        break;
                    case ';':
                        declareVar = false;
                        tokens.add(new Token(Token.Type.SEMICN, ";"));
                        break;
                    case ',':
                        tokens.add(new Token(Token.Type.COMMA, ","));
                        break;
                    case '(':
                        declareVar = false;
                        tokens.add(new Token(Token.Type.LPARENT, "("));
                        break;
                    case ')':
                        tokens.add(new Token(Token.Type.RPARENT, ")"));
                        break;
                    case '[':
                        tokens.add(new Token(Token.Type.LBRACK, "["));
                        break;
                    case ']':
                        tokens.add(new Token(Token.Type.RBRACK, "]"));
                        break;
                    case '{':
                        tokens.add(new Token(Token.Type.LBRACE, "{"));
                        break;
                    case '}':
                        tokens.add(new Token(Token.Type.RBRACE, "}"));
                        break;
                    default:
                        break;
                }
            }
            loc++;
        }
    }

    private boolean matchWord(String word) {
        switch (word) {
            case "int":
                declareVar = true;
                tokens.add(new Token(Token.Type.INTTK, word));
                break;
            case "char":
                declareVar = true;
                tokens.add(new Token(Token.Type.CHARTK, word));
                break;
            case "void":
                declareVar = true;
                tokens.add(new Token(Token.Type.VOIDTK, word));
                break;
            case "main":
                tokens.add(new Token(Token.Type.MAINTK, word));
                break;
            case "const":
                tokens.add(new Token(Token.Type.CONSTTK, word));
                break;
            case "break":
                tokens.add(new Token(Token.Type.BREAKTK, word));
                break;
            case "continue":
                tokens.add(new Token(Token.Type.CONTINUETK, word));
                break;
            case "if":
                tokens.add(new Token(Token.Type.IFTK, word));
                break;
            case "else":
                tokens.add(new Token(Token.Type.ELSETK, word));
                break;
            case "for":
                tokens.add(new Token(Token.Type.FORTK, word));
                break;
            case "getint":
                tokens.add(new Token(Token.Type.GETINTTK, word));
                break;
            case "getchar":
                tokens.add(new Token(Token.Type.GETCHARTK, word));
                break;
            case "printf":
                tokens.add(new Token(Token.Type.PRINTFTK, word));
                break;
            case "return":
                tokens.add(new Token(Token.Type.RETURNTK, word));
                break;
            default:
                return false;
        }
        return true;
    }

    public void printTokens() {
        StringBuilder output = new StringBuilder();
        if (!errors.isEmpty()) {
            for (Token error : errors) {
                output.append(error.toString()).append("\n");
            }
            try (PrintWriter writer = new PrintWriter("error.txt")) {
                writer.println(output);
            } catch (FileNotFoundException ignored) {}
        } else {
            for (Token token : tokens) {
                output.append(token.toString()).append("\n");
            }
            try (PrintWriter writer = new PrintWriter("lexer.txt")) {
                writer.println(output);
            } catch (FileNotFoundException ignored) {}
        }
    }
}

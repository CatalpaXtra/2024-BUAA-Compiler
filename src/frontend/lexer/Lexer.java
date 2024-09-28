package frontend.lexer;

import frontend.Error;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Lexer {
    private final ArrayList<Token> tokens;
    private final ArrayList<Error> errors;
    private boolean annotate;
    private static final Map<String, Token.Type> reserveWords = Map.ofEntries(
            Map.entry("main", Token.Type.MAINTK),
            Map.entry("const", Token.Type.CONSTTK),
            Map.entry("int", Token.Type.INTTK),
            Map.entry("char", Token.Type.CHARTK),
            Map.entry("break", Token.Type.BREAKTK),
            Map.entry("continue", Token.Type.CONTINUETK),
            Map.entry("if", Token.Type.IFTK),
            Map.entry("else", Token.Type.ELSETK),
            Map.entry("for", Token.Type.FORTK),
            Map.entry("getint", Token.Type.GETINTTK),
            Map.entry("getchar", Token.Type.GETCHARTK),
            Map.entry("printf", Token.Type.PRINTFTK),
            Map.entry("return", Token.Type.RETURNTK),
            Map.entry("void", Token.Type.VOIDTK)
    );

    public Lexer(Scanner scanner) {
        tokens = new ArrayList<>();
        errors = new ArrayList<>();
        annotate = false;

        int line = 1;
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            lexer(input, line++);
        }
    }

    public void lexer(String input, int line) {
        int loc = 0;
        while (loc < input.length()) {
            if (annotate) {
                if (input.charAt(loc) == '*' && loc + 1 < input.length() && input.charAt(loc + 1) == '/') {
                    annotate = false;
                    loc++;
                }
                loc++;
                continue;
            }

            if (input.charAt(loc) == ' ' || input.charAt(loc) == '\n') {
                loc++;
                continue;
            } else if (input.charAt(loc) == '/' && loc + 1 < input.length() && input.charAt(loc + 1) == '/') {
                break;
            } else if (input.charAt(loc) == '/' && loc + 1 < input.length() && input.charAt(loc + 1) == '*') {
                annotate = true;
                loc += 2;
                continue;
            }

            StringBuilder content = new StringBuilder();
            if (isCharacter(input.charAt(loc))) {
                content.append(input.charAt(loc));
                loc++;
                while (loc < input.length() && (isCharacter(input.charAt(loc)) || isDigit(input.charAt(loc)))) {
                    content.append(input.charAt(loc));
                    loc++;
                }
                Token.Type type = reserveWords.getOrDefault(content.toString(), Token.Type.IDENFR);
                tokens.add(new Token(type, content.toString(), line));
                loc--;
            } else if (isDigit(input.charAt(loc))) {
                content.append(input.charAt(loc));
                loc++;
                while (loc < input.length() && isDigit(input.charAt(loc))) {
                    content.append(input.charAt(loc));
                    loc++;
                }
                tokens.add(new Token(Token.Type.INTCON, content.toString(), line));
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
                tokens.add(new Token(Token.Type.CHRCON, content.toString(), line));
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
                tokens.add(new Token(Token.Type.STRCON, content.toString(), line));
            } else {
                switch (input.charAt(loc)) {
                    case '&':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '&') {
                            tokens.add(new Token(Token.Type.AND, "&&", line));
                        } else {
                            errors.add(new Error(Error.Type.a, "&", line));
                            loc--;
                        }
                        break;
                    case '|':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '|') {
                            tokens.add(new Token(Token.Type.OR, "||", line));
                        } else {
                            errors.add(new Error(Error.Type.a, "|", line));
                            loc--;
                        }
                        break;
                    case '!':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '=') {
                            tokens.add(new Token(Token.Type.NEQ, "!=", line));
                        } else {
                            tokens.add(new Token(Token.Type.NOT, "!", line));
                            loc--;
                        }
                        break;
                    case '<':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '=') {
                            tokens.add(new Token(Token.Type.LEQ, "<=", line));
                        } else {
                            tokens.add(new Token(Token.Type.LSS, "<", line));
                            loc--;
                        }
                        break;
                    case '>':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '=') {
                            tokens.add(new Token(Token.Type.GEQ, ">=", line));
                        } else {
                            tokens.add(new Token(Token.Type.GRE, ">", line));
                            loc--;
                        }
                        break;
                    case '=':
                        loc++;
                        if (loc < input.length() && input.charAt(loc) == '=') {
                            tokens.add(new Token(Token.Type.EQL, "==", line));
                        } else {
                            tokens.add(new Token(Token.Type.ASSIGN, "=", line));
                            loc--;
                        }
                        break;
                    case '+':
                        tokens.add(new Token(Token.Type.PLUS, "+", line));
                        break;
                    case '-':
                        tokens.add(new Token(Token.Type.MINU, "-", line));
                        break;
                    case '*':
                        tokens.add(new Token(Token.Type.MULT, "*", line));
                        break;
                    case '/':
                        tokens.add(new Token(Token.Type.DIV, "/", line));
                        break;
                    case '%':
                        tokens.add(new Token(Token.Type.MOD, "%", line));
                        break;
                    case ';':
                        tokens.add(new Token(Token.Type.SEMICN, ";", line));
                        break;
                    case ',':
                        tokens.add(new Token(Token.Type.COMMA, ",", line));
                        break;
                    case '(':
                        tokens.add(new Token(Token.Type.LPARENT, "(", line));
                        break;
                    case ')':
                        tokens.add(new Token(Token.Type.RPARENT, ")", line));
                        break;
                    case '[':
                        tokens.add(new Token(Token.Type.LBRACK, "[", line));
                        break;
                    case ']':
                        tokens.add(new Token(Token.Type.RBRACK, "]", line));
                        break;
                    case '{':
                        tokens.add(new Token(Token.Type.LBRACE, "{", line));
                        break;
                    case '}':
                        tokens.add(new Token(Token.Type.RBRACE, "}", line));
                        break;
                    default:
                        break;
                }
            }
            loc++;
        }
    }

    private boolean isCharacter(char ch) {
        return ch == '_' || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public void printTokens() {
        StringBuilder output = new StringBuilder();
        if (!errors.isEmpty()) {
            for (Error error : errors) {
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

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public ArrayList<Error> getErrors() {
        return errors;
    }
}

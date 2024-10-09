import frontend.CompErrors;
import frontend.lexer.Lexer;
import frontend.parser.CompUnit;
import frontend.parser.Parser;
import frontend.parser.ParserErrors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("testfile.txt"));
        } catch (FileNotFoundException ignored) {}

        Lexer lexer = new Lexer(scanner);
        Parser parser = new Parser(lexer);
        CompUnit compUnit = parser.parse();

        CompErrors compErrors = new CompErrors(lexer.getErrors(), ParserErrors.getErrors());
        if (compErrors.existError()) {
            String output = compErrors.toString();
            try (PrintWriter writer = new PrintWriter("error.txt")) {
                writer.println(output);
            } catch (FileNotFoundException ignored) {}
        } else {
            String output = compUnit.toString();
            try (PrintWriter writer = new PrintWriter("parser.txt")) {
                writer.println(output);
            } catch (FileNotFoundException ignored) {}
        }
    }
}
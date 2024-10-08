import frontend.lexer.Lexer;
import frontend.parser.CompUnit;
import frontend.parser.Parser;

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

        String output = compUnit.toString();
        try (PrintWriter writer = new PrintWriter("lexer.txt")) {
            writer.println(output);
        } catch (FileNotFoundException ignored) {}
    }
}
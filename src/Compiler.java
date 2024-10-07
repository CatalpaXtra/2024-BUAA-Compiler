import frontend.lexer.Lexer;
import frontend.parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("testfile.txt"));
        } catch (FileNotFoundException ignored) {}

        Lexer lexer = new Lexer(scanner);
        Parser parser = new Parser(lexer);
        parser.parseDecls();
        parser.print();
    }
}
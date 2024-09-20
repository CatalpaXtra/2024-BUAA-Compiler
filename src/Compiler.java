import frontend.Lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) {
        try {
            File inputFile = new File("testfile.txt");

            Scanner scanner = new Scanner(inputFile);
            Lexer lexer = new Lexer();
            int line = 1;
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                lexer.lexer(input, line++);
            }
            lexer.printTokens();

            scanner.close();
        } catch (FileNotFoundException ignored) {}
    }
}
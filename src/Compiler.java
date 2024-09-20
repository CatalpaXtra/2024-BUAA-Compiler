import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Lexer lexer = new Lexer();
        int line = 1;
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            lexer.lexer(input, line++);
        }
        lexer.printTokens();
    }
}
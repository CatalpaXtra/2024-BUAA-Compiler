import frontend.lexer.Lexer;
import frontend.lexer.LexerErrors;
import frontend.parser.CompUnit;
import frontend.parser.Parser;
import frontend.parser.ParserErrors;
import midend.Semantic;
import midend.SemanticErrors;
import midend.llvm.Builder;
import midend.llvm.Module;

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
        Semantic semantic = new Semantic(compUnit);
        semantic.visit();

        CompErrors compErrors = new CompErrors(LexerErrors.getErrors(), ParserErrors.getErrors(), SemanticErrors.getErrors());
        if (compErrors.existError()) {
            try (PrintWriter writer = new PrintWriter("error.txt")) {
                writer.println(compErrors.toString());
            } catch (FileNotFoundException ignored) {}
            return;
        }

        Builder builder = new Builder(compUnit);
        builder.build();
        Module module = builder.getModule();
        try (PrintWriter writer = new PrintWriter("llvm_ir.txt")) {
            writer.println(module.irOut());
        } catch (FileNotFoundException ignored) {}
    }
}
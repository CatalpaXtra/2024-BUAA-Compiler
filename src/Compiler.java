import frontend.lexer.Lexer;
import frontend.lexer.LexerErrors;
import frontend.parser.CompUnit;
import frontend.parser.Parser;
import frontend.parser.ParserErrors;
import midend.semantic.Semantic;
import midend.semantic.SemanticErrors;
import midend.llvm.IrBuilder;
import midend.llvm.IrModule;

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

        IrBuilder irBuilder = new IrBuilder(compUnit);
        irBuilder.build();
        try (PrintWriter writer = new PrintWriter("llvm_ir.txt")) {
            writer.println(IrModule.irOut());
        } catch (FileNotFoundException ignored) {}
    }
}
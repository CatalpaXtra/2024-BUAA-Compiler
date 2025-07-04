import backend.Builder;
import backend.Module;
import frontend.lexer.Lexer;
import frontend.lexer.LexerErrors;
import frontend.parser.CompUnit;
import frontend.parser.Parser;
import frontend.parser.ParserErrors;
import midend.optimizer.Optimizer;
import midend.optimizer.PeepHole;
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
        } else {
            try (PrintWriter writer = new PrintWriter("lexer.txt")) {
                writer.println(lexer.toString());
            } catch (FileNotFoundException ignored) {}
            try (PrintWriter writer = new PrintWriter("parser.txt")) {
                writer.println(compUnit.toString());
            } catch (FileNotFoundException ignored) {}
            try (PrintWriter writer = new PrintWriter("symbol.txt")) {
                writer.println(semantic.toString());
            } catch (FileNotFoundException ignored) {}
        }

        IrBuilder irBuilder = new IrBuilder(compUnit);
        irBuilder.build();
        Optimizer.optimize();
        try (PrintWriter writer = new PrintWriter("llvm_ir.txt")) {
            writer.println(IrModule.irOut());
        } catch (FileNotFoundException ignored) {}

        Builder builder = new Builder();
        builder.build();
        PeepHole.optimize();
        try (PrintWriter writer = new PrintWriter("mips.txt")) {
            writer.println(Module.mipsOut());
        } catch (FileNotFoundException ignored) {}
    }
}
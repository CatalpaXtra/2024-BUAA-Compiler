package frontend.parser.function;

import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.block.Block;
import frontend.parser.block.BlockParser;

public class MainFuncDefParser {
    private final TokenIterator iterator;
    private Token intTk;
    private Token mainTk;
    private Token lParent;
    private Token rParent;
    private Block block;

    public MainFuncDefParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public MainFuncDef parseMainFuncDef() {
        intTk = iterator.getNextToken();
        mainTk = iterator.getNextToken();
        lParent = iterator.getNextToken();
        ErrorHandler errorHandler = new ErrorHandler(iterator);
        rParent = errorHandler.handleErrorJ();
        BlockParser blockParser = new BlockParser(iterator);
        block = blockParser.parseBlock();
        MainFuncDef mainFuncDef = new MainFuncDef(intTk, mainTk, lParent, rParent, block);
        return mainFuncDef;
    }
}

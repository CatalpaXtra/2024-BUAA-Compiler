package frontend.parser.function;

import frontend.Error;
import frontend.ErrorHandler;
import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.ParserErrors;
import frontend.parser.block.Block;
import frontend.parser.block.BlockParser;
import frontend.parser.function.params.FuncFParams;
import frontend.parser.function.params.FuncFParamsParser;
import frontend.parser.terminal.Ident;
import frontend.parser.terminal.IdentParser;

public class FuncDefParser {
    private final TokenIterator iterator;
    private FuncType funcType;
    private Ident ident;
    private Token lParent;
    private FuncFParams funcFParams;
    private Token rParent;
    private Block block;

    public FuncDefParser(TokenIterator iterator) {
        this.iterator = iterator;
    }

    public FuncDef parseFuncDef() {
        FuncTypeParser funcTypeParser = new FuncTypeParser(iterator);
        funcType = funcTypeParser.parseFuncType();
        IdentParser identParser = new IdentParser(iterator);
        ident = identParser.parseIdent();
        lParent = iterator.getNextToken();

        Token token = iterator.getNextToken();
        if (token.getType().equals(Token.Type.RPARENT)) {
            funcFParams = null;
            rParent = token;
        } else if (token.getType().equals(Token.Type.LBRACE)) {
            funcFParams = null;
            iterator.traceBack(2);
            token = iterator.getNextToken();
            Error error = new Error(Error.Type.j, ")", token.getLine());
            ParserErrors.addError(error);
            rParent = new Token(Token.Type.RPARENT, ")", token.getLine());
        } else {
            iterator.traceBack(1);
            FuncFParamsParser funcFParamsParser = new FuncFParamsParser(iterator);
            funcFParams = funcFParamsParser.parseFuncFParams();
            ErrorHandler errorHandler = new ErrorHandler(iterator);
            rParent = errorHandler.handleErrorJ();
        }
        BlockParser blockParser = new BlockParser(iterator);
        block = blockParser.parseBlock();
        FuncDef funcDef = new FuncDef(funcType, ident, lParent, funcFParams, rParent, block);
        return funcDef;
    }
}

package frontend.parser.function;

import frontend.lexer.Token;
import frontend.lexer.TokenIterator;
import frontend.parser.block.Block;
import frontend.parser.block.BlockParser;
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
        } else {
            iterator.traceBack(1);
            FuncFParamsParser funcFParamsParser = new FuncFParamsParser(iterator);
            funcFParams = funcFParamsParser.parseFuncFParams();
            rParent = iterator.getNextToken();
        }

        BlockParser blockParser = new BlockParser(iterator);
        block = blockParser.parseBlock();

        FuncDef funcDef = new FuncDef(funcType, ident, lParent, funcFParams, rParent, block);
        return funcDef;
    }
}

package midend.llvm.symbol;

import frontend.parser.function.params.FuncFParam;

import java.util.ArrayList;

public class SymbolFunc extends Symbol {
    private final ArrayList<Symbol> symbols;
    private final ArrayList<FuncFParam> funcFParams;

    public SymbolFunc(String symbolType, String name, ArrayList<FuncFParam> funcFParams) {
        super(symbolType, name, "", -1);
        this.symbols =  new ArrayList<>();
        this.funcFParams = funcFParams;
    }

    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }
}

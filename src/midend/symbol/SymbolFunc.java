package midend.symbol;

import frontend.parser.function.params.FuncFParam;
import frontend.parser.function.params.FuncFParams;

import java.util.ArrayList;

public class SymbolFunc extends Symbol {
    private final ArrayList<Symbol> symbols;
    private final ArrayList<FuncFParam> funcFParams;

    public SymbolFunc(String symbolType, String name, int line, int scope, ArrayList<FuncFParam> funcFParams) {
        super(symbolType, name, line, scope);
        this.symbols =  new ArrayList<>();
        this.funcFParams = funcFParams;
    }

    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);
    }

    public int getSymbolNum() {
        return symbols.size();
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }
}

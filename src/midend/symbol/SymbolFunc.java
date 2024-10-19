package midend.symbol;

import java.util.ArrayList;

public class SymbolFunc extends Symbol {
    private final ArrayList<Symbol> symbols;

    public SymbolFunc(String symbolType, String name, int line, int scope) {
        super(symbolType, name, line, scope);
        this.symbols =  new ArrayList<>();
    }

    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);
    }

    public int getSymbolNum() {
        return symbols.size();
    }

    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }
}

package midend.llvm.symbol;

import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, Symbol> symbols;
    private final SymbolTable parent;

    public SymbolTable() {
        this.symbols = new HashMap<>();
        this.parent = null;
    }

    public SymbolTable(SymbolTable parent) {
        this.symbols = new HashMap<>();
        this.parent = parent;
    }

    public void addSymbol(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }

    public boolean hasParent() {
        return parent != null;
    }

    public Symbol getSymbol(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        if (hasParent()) {
            return parent.getSymbol(name);
        }
        return null;
    }

}

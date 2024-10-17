package midend.symbol;

import midend.ErrorHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, Symbol> symbols;
    private final ArrayList<Symbol> symbolList;
    private final SymbolTable parent;
    private final int depth;

    public SymbolTable() {
        this.symbols = new HashMap<>();
        this.symbolList = new ArrayList<>();
        this.parent = null;
        this.depth = 0;
    }

    public SymbolTable(SymbolTable parent) {
        this.symbols = new HashMap<>();
        this.symbolList = new ArrayList<>();
        this.parent = parent;
        this.depth = parent.getDepth() + 1;
    }

    public void addSymbol(Symbol symbol) {
        if (!ErrorHandler.handleErrorB(symbol, symbols)) {
            symbols.put(symbol.getName(), symbol);
            symbolList.add(symbol);
        }
    }

    private int getDepth() {
        return depth;
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

    public ArrayList<Symbol> getSymbolList() {
        return symbolList;
    }

}

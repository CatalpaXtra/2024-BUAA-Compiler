package midend.symbol;

public class Symbol {
    private final String symbolType;
    private final String name;
    private final int line;
    private final int scope; // 作用域序号

    public Symbol(String symbolType, String name, int line, int scope) {
        this.symbolType = symbolType;
        this.name = name;
        this.line = line;
        this.scope = scope;
    }

    public String getSymbolType() {
        return symbolType;
    }

    public String getName() {
        return name;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return scope + " " + name + " " + symbolType;
    }

}

package midend.symbol;

public class Symbol {
    private final String symbolType;
    private final String name;
    private final int line;
    private final int scope; // 作用域序号
    private final String memory;
    private final int arraySize;

    public Symbol(String symbolType, String name, int line, int scope) {
        this.symbolType = symbolType;
        this.name = name;
        this.line = line;
        this.scope = scope;
        this.memory = "";
        this.arraySize = -1;
    }

    public Symbol(String symbolType, String name, int line, int scope, String memory, int arraySize) {
        this.symbolType = symbolType;
        this.name = name;
        this.line = line;
        this.scope = scope;
        this.memory = memory;
        this.arraySize = arraySize;
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

    public String getMemory() {
        return memory;
    }

    public int getArraySize() {
        return arraySize;
    }

    public boolean isArray() {
        return symbolType.contains("Array");
    }

    public boolean isPointer() {
        return symbolType.contains("Pointer");
    }

    public boolean isChar() {
        return symbolType.contains("Char");
    }

    @Override
    public String toString() {
        return scope + " " + name + " " + symbolType;
    }

}

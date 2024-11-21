package midend.llvm.symbol;

import midend.llvm.Value;

public class Symbol {
    private final String symbolType;
    private final String name;
    private final Value memory;
    private final int arraySize;

    public Symbol(String symbolType, String name, Value memory, int arraySize) {
        this.symbolType = symbolType;
        this.name = name;
        this.memory = memory;
        this.arraySize = arraySize;
    }

    public String getSymbolType() {
        return symbolType;
    }

    public String getName() {
        return name;
    }

    public Value getMemory() {
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
        return name + " " + symbolType;
    }

}

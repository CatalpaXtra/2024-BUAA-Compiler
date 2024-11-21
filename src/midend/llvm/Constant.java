package midend.llvm;

public class Constant extends Value {
    private final int value;

    public Constant(int value){
        super(value, "i32");
        this.value = value;
    }

    public String irOut() {
        return value + "";
    }

    public int getValue() {
        return value;
    }
}

package backend.global;

import java.util.ArrayList;

public class AsmByte extends AsmGlobal {
    private final ArrayList<Integer> initVal;
    private final int size;

    public AsmByte(String name, int size, ArrayList<Integer> initial) {
        super(name);
        this.initVal = initial;
        this.size = size;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(name + ": .byte ");
        if (initVal.isEmpty()) {
            sb.append("0:").append(size);
        } else {
            for (int i = 0; i < initVal.size(); i++){
                if (i == 0) {
                    sb.append(initVal.get(i));
                } else {
                    sb.append(", ").append(initVal.get(i));
                }
            }
        }
        return sb.toString();
    }
}

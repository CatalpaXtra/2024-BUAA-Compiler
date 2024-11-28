package backend.global;

import java.util.ArrayList;

public class AsmWord extends AsmGlobal {
    private final ArrayList<Integer> initVal;
    private final int size;

    public AsmWord(String name, int size, ArrayList<Integer> initial) {
        super(name);
        this.size = size;
        this.initVal = initial;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(name + ": .word ");
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
            for (int i = initVal.size(); i < size; i++) {
                sb.append(", ").append(0);
            }
        }
        return sb.toString();
    }
}

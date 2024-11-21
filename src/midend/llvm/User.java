package midend.llvm;

import java.util.ArrayList;

public class User extends Value {
    private ArrayList<Value> operands;

    public User(String name, String type) {
        super(name, type);
        this.operands = new ArrayList<>();
    }

    public void addOperand(Value value){
        operands.add(value);
        if (value != null) {
            value.addUser(this);
        }
    }

    public void modifyValue(Value oldValue,Value newValue){
        while(true) {
            int index = operands.indexOf(oldValue);
            if (index == -1) {
                break;
            }
            operands.set(index,newValue);
            newValue.addUser(this);
        }
    }

    public void removeOperands(){
        for(Value value:operands){
            if(value != null){
                value.deleteUser(this);
            }
        }
    }

    public void setOperands(Value value,int pos) {
        this.operands.set(pos,value);
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }
}

package midend.llvm;

import java.util.ArrayList;

public class Value {
    protected int regNum;
    protected final String name;
    protected String irType;
    protected ArrayList<User> users;

    public Value(int regNum, String irType) {
        this.regNum = regNum;
        this.name = "%" + regNum;
        this.irType = irType;
        this.users = new ArrayList<>();
    }

    public Value(String name, String irType) {
        this.name = name;
        this.irType = irType;
        this.users = new ArrayList<>();
    }

    public boolean isValue() {
        return irType.equals("i32");
    }

    public boolean isCondValue() {
        return irType.equals("i1");
    }

    public String getName() {
        return name;
    }

    public String getIrType() {
        return irType;
    }

    public void addUser(User user){
        if(!users.contains(user)){
            users.add(user);
        }
    }

    public ArrayList<User> getUsers(){
        return users;
    }

    public void modifyValueForUsers(Value newValue){
        for(User user:users){
            user.modifyValue(this, newValue);
        }
        users = new ArrayList<>();
    }

    public void deleteUser(User user){
        users.remove(user);
    }
}

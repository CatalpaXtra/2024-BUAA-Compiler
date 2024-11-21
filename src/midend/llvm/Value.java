package midend.llvm;

import java.util.ArrayList;

public class Value {
    private int regNum;
    private final String name;
    private final String type;
    private ArrayList<User> users;

    public Value(int regNum, String type) {
        this.regNum = regNum;
        this.name = "%" + regNum;
        this.type = type;
        this.users = new ArrayList<>();
    }

    public Value(String name, String type) {
        this.name = name;
        this.type = type;
        this.users = new ArrayList<>();
    }

    public boolean isValue() {
        return type.equals("i32");
    }

    public boolean isCondValue() {
        return type.equals("i1");
    }

    public String irOut() {
        return name;
    }

    public String getLlvmType() {
        return type;
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

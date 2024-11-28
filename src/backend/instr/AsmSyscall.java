package backend.instr;

public class AsmSyscall extends AsmInstr {
    public AsmSyscall() {
    }

    @Override
    public String toString(){
        return "syscall";
    }
}

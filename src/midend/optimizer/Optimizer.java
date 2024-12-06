package midend.optimizer;


public class Optimizer {
    public static boolean optimize = true;

    public static void optimize() {
        if (optimize) {
            DeadCodeRm.removeDeadCode();
            //RegAlloc.allocReg();
        }
    }
}

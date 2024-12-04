package midend.optimizer;


public class Optimizer {
    public static boolean optimize = false;

    public static void optimize() {
        if (optimize) {
            DeadCodeRm.removeDeadCode();
        }
    }
}

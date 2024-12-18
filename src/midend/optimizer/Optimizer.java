package midend.optimizer;


public class Optimizer {
    public static boolean optimize = true;

    public static void optimize() {
        SideEffectsAnalyze.analyzeSideEffects();
        if (optimize) {
            DeadCodeRm.removeDeadCode();
//            RegAlloc.allocReg();
        }
    }
}

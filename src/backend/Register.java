package backend;

public enum Register {
    zero, at, v0, v1,
    a0, a1, a2, a3,
    t0, t1, t2, t3, t4, t5, t6, t7, t8, t9,
    s0, s1, s2, s3, s4, s5, s6, s7,
    k0, k1,
    gp, sp, fp, ra,
    hi, lo;

    private static int curReg = 0;

    public static Register allocReg() {
        return getByOffset(Register.t0, curReg++);
    }

    public static void resetCurReg() {
        curReg = 0;
    }

    public static Register getByOffset(Register register, int offset) {
        return Register.values()[register.ordinal() + offset];
    }

    @Override
    public String toString() {
        return "$" + this.name();
    }
}

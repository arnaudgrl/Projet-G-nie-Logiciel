package fr.ensimag.ima.pseudocode;

/**
 * Register operand (including special registers like SP).
 *
 * @author Ensimag
 * @date 01/01/2022
 * @version $Id: $Id
 */
public class Register extends DVal {
    private String name;
    /**
     * <p>Constructor for Register.</p>
     *
     * @param name a {@link java.lang.String} object
     */
    protected Register(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Global Base register
     */
    public static final Register GB = new Register("GB");
    /**
     * Local Base register
     */
    public static final Register LB = new Register("LB");
    /**
     * Stack Pointer
     */
    public static final Register SP = new Register("SP");
    /**
     * General Purpose Registers. Array is private because Java arrays cannot be
     * made immutable, use getR(i) to access it.
     */
    private static final GPRegister[] R = initRegisters();
    /**
     * General Purpose Registers
     *
     * @param i a int
     * @return a {@link fr.ensimag.ima.pseudocode.GPRegister} object
     */
    public static GPRegister getR(int i) {
        return R[i];
    }
    /**
     * Convenience shortcut for R[0]
     */
    public static final GPRegister R0 = R[0];
    /**
     * Convenience shortcut for R[1]
     */
    public static final GPRegister R1 = R[1];
    static private GPRegister[] initRegisters() {
        GPRegister [] res = new GPRegister[16];
        for (int i = 0; i <= 15; i++) {
            res[i] = new GPRegister("R" + i, i);
        }
        return res;
    }
}

package fr.ensimag.ima.pseudocode;

/**
 * General Purpose Register operand (R0, R1, ... R15).
 *
 * @author Ensimag
 * @date 01/01/2022
 * @version $Id: $Id
 */
public class GPRegister extends Register {
    /**
     * <p>Getter for the field <code>number</code>.</p>
     *
     * @return the number of the register, e.g. 12 for R12.
     */
    public int getNumber() {
        return number;
    }

    private int number;

    GPRegister(String name, int number) {
        super(name);
        this.number = number;
    }
}

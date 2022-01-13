package fr.ensimag.deca.tree;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import org.apache.log4j.Logger;

/**
 * @author gl20
 * @date 01/01/2022
 */
public class Plus extends AbstractOpArith {
    private static final Logger LOG = Logger.getLogger(Plus.class);
    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
 

    @Override
    protected String getOperatorName() {
        return "+";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        super.codeGenInst(compiler);
        // <mnemo(op)> <dval(e2)> Rn
        LOG.debug("    Used : " + compiler.getData().getLastUsedRegister());
        compiler.addInstruction(new ADD(op1, op2));
        compiler.getData().setLastUsedRegister(op2);
    }
}

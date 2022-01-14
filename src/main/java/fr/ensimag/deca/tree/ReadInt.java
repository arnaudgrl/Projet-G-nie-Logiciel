package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.Data;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RINT;

import java.io.PrintStream;

// import org.apache.log4j.Logger;



/**
 *
 * @author gl20
 * @date 01/01/2022
 */
public class ReadInt extends AbstractReadExpr {

    // private static final Logger LOG = Logger.getLogger(AbstractBinaryExpr.class);

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                return new IntType(compiler.getSymbolTable().create("int"));
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readInt()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        Data data = compiler.getData();
        compiler.addInstruction(new RINT());
        compiler.addInstruction(new BOV(new Label("io_error")));
        // if (data.hasFreeRegister()) {
        GPRegister lastRegister = data.getFreeRegister(compiler);
        compiler.addInstruction(new LOAD(GPRegister.R1, lastRegister));
        data.setLastUsedRegister(lastRegister);
        // } else {
        //     // compiler.addInstruction(new PUSH(data.getMaxRegister()), "sauvegarde");
        //     // compiler.addInstruction(new LOAD(GPRegister.R1, data.getMaxRegister()));
        //     // compiler.addInstruction(new LOAD(op, GPRegister.R0));
        //     // compiler.addInstruction(new POP((GPRegister)op), "restauration");
        //     // data.decrementFreeStoragePointer();
        // }
    }

}

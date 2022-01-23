package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

public class Return extends AbstractInst {

    private AbstractExpr e;

    public Return(AbstractExpr condition) {
        Validate.notNull(condition);
        this.e = condition;
    }


    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        e.codeGenInst(compiler);
        compiler.addComment("return de la méthode");
        compiler.addInstruction(
            new LOAD(
                compiler.getData().getLastUsedRegister(),
                Register.R0
            )
        );
        compiler.addInstruction(
            new BRA(compiler.getData().getLabelReturn())
        );
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {

            if(returnType.isVoid()){
                throw new ContextualError("returned type can't be void", getLocation());
            }
            Type typeE = e.verifyExpr(compiler, localEnv, currentClass);
            Type type = compiler.assignCompatible(compiler, typeE, returnType);
            if (type == null) {
                throw new ContextualError("unexpected return type", e.getLocation());
            }
            e.setType(type);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        e.decompile(s);
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        e.iter(f);

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        e.prettyPrint(s, prefix, true);
    }

    
}

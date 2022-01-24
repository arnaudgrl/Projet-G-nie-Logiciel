package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Full if/else if/else statement.
 *
 * @author gl20
 * @date 01/01/2022
 */
public class IfThenElse extends AbstractInst {
    
    private final AbstractExpr condition; 
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }


    public ListInst getElseBranch(){
        return this.elseBranch;
    }

    public void setElseBranch(ListInst elseBranch){
        this.elseBranch = elseBranch;
    }
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
            this.condition.verifyCondition(compiler, localEnv, currentClass);
            if(thenBranch!=null){
                this.thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
            }
            if(elseBranch!= null){
                this.elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
            }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        Label E_else = new Label("E_Else."+compiler.getNLabel());
        Label E_end = new Label("E_End."+compiler.getNLabel());
        compiler.incrNLabel();
        condition.codeBoolean(false, E_else, compiler);
        thenBranch.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(E_end));
        compiler.addLabel(E_else);
        elseBranch.codeGenListInst(compiler);
        compiler.addLabel(E_end);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("if (");
        condition.decompile(s);
        s.print(") ");
        if(thenBranch != null){
            s.println("{");
            s.indent();
            thenBranch.decompile(s);
            s.unindent();
        }
        if (elseBranch != null) {
            s.println("} else {");
            s.indent();
            elseBranch.decompile(s);
            s.unindent();
            s.println("}");
        }
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }
}

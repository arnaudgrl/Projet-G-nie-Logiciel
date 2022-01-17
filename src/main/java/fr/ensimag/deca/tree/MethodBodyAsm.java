package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;


public class MethodBodyAsm extends AbstractExpr {
    private StringLiteral code;

    public MethodBodyAsm(StringLiteral string){
        this.code= string;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("asm(");
        s.print(code.decompile());
        s.print(");");
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        code.prettyPrint(s, prefix, false);
    }


    @Override
    protected void iterChildren(TreeFunction f) {
        code.iter(f);
    }

    
}

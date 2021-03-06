package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.Data;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl20
 * @date 01/01/2022
 * @version $Id: $Id
 */
public abstract class AbstractExpr extends AbstractInst {

    /**
     * @return true if the expression does not correspond to any concrete token
     * in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed by contextual verification).
     *
     * @return a {@link fr.ensimag.deca.context.Type} object
     */
    public Type getType() {
        return type;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type a {@link fr.ensimag.deca.context.Type} object
     */
    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }
    private Type type;

    /** {@inheritDoc} */
    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     *
     * implements non-terminals "expr" and "lvalue"
     *    of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  (contains the "env_types" attribute)
     * @param localEnv
     *            Environment in which the expression should be checked
     *            (corresponds to the "env_exp" attribute)
     * @param currentClass
     *            Definition of the class containing the expression
     *            (corresponds to the "class" attribute)
     *             is null in the main bloc.
     * @return the Type of the expression
     *            (corresponds to the "type" attribute)
     * @throws fr.ensimag.deca.context.ContextualError if any.
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments
     *
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute
     * @return this with an additional ConvFloat if needed...
     * @throws fr.ensimag.deca.context.ContextualError if any.
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, 
            Type expectedType)
            throws ContextualError {
            Type type = this.verifyExpr(compiler, localEnv, currentClass);
            if(type.isInt() && expectedType.isFloat()){
                ConvFloat cf = new ConvFloat(this);
                Type typeCf = cf.verifyExpr(compiler, localEnv, currentClass);
                cf.setType(typeCf);
                return cf;
            }
            if (compiler.assignCompatible(compiler, expectedType, type) != null) {
                return this;
            }
        throw new ContextualError("Assignment error", getLocation());
    }
    
    
    /** {@inheritDoc} */
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        Type type = this.verifyExpr(compiler, localEnv, currentClass);
        this.setType(type);
        
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *            Environment in which the condition should be checked.
     * @param currentClass
     *            Definition of the class containing the expression, or null in
     *            the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
    	setType(verifyExpr(compiler, localEnv, currentClass));	
	 	if (!(this.verifyExpr(compiler, localEnv, currentClass).getName().getName().equals("boolean")))
		    	throw new ContextualError ( "condition type must be boolean",this.getLocation());
    }

    /**
     * Generate code to print the expression
     *
     * @param compiler a {@link fr.ensimag.deca.DecacCompiler} object
     * @param printHex a boolean
     */
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        this.codeGenInst(compiler);
        Data data = compiler.getData();
        GPRegister register = data.getLastUsedRegister();
        compiler.addInstruction(new LOAD(register, Register.R1));
        if (getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else if (getType().isFloat()) {
            if (printHex) {
                compiler.addInstruction(new WFLOATX());
            } else {
                compiler.addInstruction(new WFLOAT());
            }
        } 
    }

    /** {@inheritDoc} */
    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }
    

    /** {@inheritDoc} */
    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    /** {@inheritDoc} */
    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }

    /**
     * <p>getDVal.</p>
     *
     * @param <Optional> a Optional class
     * @return a {@link fr.ensimag.ima.pseudocode.DVal} object
     */
    protected <Optional>DVal getDVal() {
        return null;
    }

    /**
     * <p>codeBoolean.</p>
     *
     * @param b a boolean
     * @param E a {@link fr.ensimag.ima.pseudocode.Label} object
     * @param compiler a {@link fr.ensimag.deca.DecacCompiler} object
     */
    protected void codeBoolean(boolean b, Label E, DecacCompiler compiler) {}

    /**
     * <p>codeGenAssign.</p>
     *
     * @param compiler a {@link fr.ensimag.deca.DecacCompiler} object
     * @param register a {@link fr.ensimag.ima.pseudocode.Register} object
     */
    protected void codeGenAssign(DecacCompiler compiler, Register register) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * <p>codeGenSelect.</p>
     *
     * @param compiler a {@link fr.ensimag.deca.DecacCompiler} object
     */
    protected void codeGenSelect(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}


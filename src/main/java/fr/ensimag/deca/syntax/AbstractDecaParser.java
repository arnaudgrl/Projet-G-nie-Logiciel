package fr.ensimag.deca.syntax;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.deca.tree.Tree;

import java.io.PrintStream;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.log4j.Logger;

/**
 * The super class of the generated parser. It is extended by the generated
 * code because of the superClass option in the .g file.
 *
 * @author gl20, Based on template by Jim Idle - Temporal Wave LLC - jimi@temporal-wave.com
 * @date 01/01/2022
 * @version $Id: $Id
 */
public abstract class AbstractDecaParser extends Parser {
    Logger LOG = Logger.getLogger(AbstractDecaParser.class);

    private DecacCompiler decacCompiler;

    /**
     * <p>Getter for the field <code>decacCompiler</code>.</p>
     *
     * @return a {@link fr.ensimag.deca.DecacCompiler} object
     */
    protected DecacCompiler getDecacCompiler() {
        return decacCompiler;
    }

    /**
     * <p>Setter for the field <code>decacCompiler</code>.</p>
     *
     * @param decacCompiler a {@link fr.ensimag.deca.DecacCompiler} object
     */
    public void setDecacCompiler(DecacCompiler decacCompiler) {
        this.decacCompiler = decacCompiler;
    }

    /**
     * <p>parseProgram.</p>
     *
     * @return a {@link fr.ensimag.deca.tree.AbstractProgram} object
     */
    protected abstract AbstractProgram parseProgram();
    
    /**
     * <p>parseProgramAndManageErrors.</p>
     *
     * @param err a {@link java.io.PrintStream} object
     * @return a {@link fr.ensimag.deca.tree.AbstractProgram} object
     */
    public AbstractProgram parseProgramAndManageErrors(PrintStream err) {
        try {
            AbstractProgram result = parseProgram();
            assert(result != null);
            return result;
        } catch (ParseCancellationException e) {
            LOG.debug("ParseCancellationException raised while compiling file:", e);
            if (e.getCause() instanceof LocationException) {
                ((LocationException)e.getCause()).display(err);
                return null;
            } else {
                throw new DecacInternalError("Parsing cancelled", e);
            }
        } catch (DecaRecognitionException e) {
            e.display(err);
            return null;
        }
    }

    /**
     * Extract the Location of a token.
     *
     * @param token a {@link org.antlr.v4.runtime.Token} object
     * @return a {@link fr.ensimag.deca.tree.Location} object
     */
    protected Location tokenLocation(Token token) {
        return new Location(token.getLine(),
                token.getCharPositionInLine(),
                token.getInputStream().getSourceName());
    }
    
    /**
     * Sets the location of Tree to the one in Token.
     *
     * This is a simple convenience wrapper around {@link fr.ensimag.deca.tree.Tree#setLocation(Location)}.
     *
     * @param tree a {@link fr.ensimag.deca.tree.Tree} object
     * @param token a {@link org.antlr.v4.runtime.Token} object
     */
    protected void setLocation(Tree tree, Token token) {
        tree.setLocation(tokenLocation(token));
    }

    /**
     * Create a new parser instance, pre-supplying the input token stream.
     *
     * @param input The stream of tokens that will be pulled from the lexer
     */
    protected AbstractDecaParser(TokenStream input) {
        super(input);
        setErrorHandler(new DefaultErrorStrategy() {
            @Override
            public void reportError(Parser recognizer,
                                    RecognitionException e) {
                LOG.debug("Error found by ANTLR");
                if (e instanceof DecaRecognitionException) {
                    Token offendingToken = e.getOffendingToken();
                    if (offendingToken == null) {
                        offendingToken = recognizer.getCurrentToken();                        
                    }
                    recognizer.notifyErrorListeners(offendingToken, e.getMessage(), e);
                } else {
                    super.reportError(recognizer, e);
                }
            }
        });
        removeErrorListeners();
        addErrorListener(new DecacErrorListner(input));
    }
}


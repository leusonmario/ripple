package net.fortytwo.ripple.libs.logic;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.libs.stack.StackLibrary;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.StackMapping;

/**
 * A primitive which consumes a Boolean value and produces its inverse.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Not extends PrimitiveStackMapping {
    private static final String[] IDENTIFIERS = {
            LogicLibrary.NS_2013_03 + "not",
            LogicLibrary.NS_2008_08 + "not",
            StackLibrary.NS_2007_08 + "not",
            StackLibrary.NS_2007_05 + "not"};

    public String[] getIdentifiers() {
        return IDENTIFIERS;
    }

    public Not()
            throws RippleException {
        super();
    }

    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("x", "a boolean value (xsd:true or xsd:false)", true)};
    }

    public String getComment() {
        return "x  =>  y  -- where y is true if x is false, otherwise false";
    }

    public void apply(final RippleList arg,
                      final Sink<RippleList> solutions,
                      final ModelConnection mc) throws RippleException {

        RippleList stack = arg;
        boolean x;

        x = mc.toBoolean(stack.getFirst());
        stack = stack.getRest();

        Object result = !x;

        solutions.put(
                stack.push(result));
    }

    @Override
    public StackMapping getInverse() {
        return this;
    }
}


package net.fortytwo.ripple.libs.system;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.libs.math.MathLibrary;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;

/**
 * A primitive which produces a random number between 0 and 1.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Random extends PrimitiveStackMapping {
    public String[] getIdentifiers() {
        return new String[]{
                SystemLibrary.NS_2013_03 + "random",
                MathLibrary.NS_2008_08 + "random",
                MathLibrary.NS_2007_08 + "random"};
    }

    public Random()
            throws RippleException {
        super();
    }

    public Parameter[] getParameters() {
        return new Parameter[]{};
    }

    public String getComment() {
        return "=> x, where x is a pseudorandom number in the interval [0.0, 1.0)";
    }

    public void apply(final RippleList arg,
                      final Sink<RippleList> solutions,
                      final ModelConnection mc) throws RippleException {
        RippleList stack = arg;

        Number result;

        result = Math.random();

        solutions.put(
                stack.push(result));
    }
}


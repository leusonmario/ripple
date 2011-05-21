/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2011 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.control;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.Operator;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.StackContext;
import net.fortytwo.ripple.model.StackMappingWrapper;
import net.fortytwo.ripple.model.regex.TimesQuantifier;
import net.fortytwo.flow.Sink;

/**
 * A primitive which consumes an item and a number n, then pushes n active
 * copies of the item to the stack.  This has the effect of applying the
 * filter "n times" to the remainder of the stack.
 */
public class TimesApply extends PrimitiveStackMapping {
    public String[] getIdentifiers() {
        return new String[]{
                // Note: this primitive has different semantics than its predecessors, stack:times and stack:timesApply
                ControlLibrary.NS_2011_04 + "times-apply"};
    }

    public TimesApply() throws RippleException {
        super();
    }

    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("p", "the program the be executed", true),
                new Parameter("n", "the number of times to execute p", true)};
    }

    public String getComment() {
        return "p n  =>  p{n}  -- pushes n active copies of the program p, or 'executes p n times'";
    }

    public void apply(final StackContext arg,
                      final Sink<StackContext, RippleException> solutions)
            throws RippleException {
        RippleList stack = arg.getStack();
        final ModelConnection mc = arg.getModelConnection();

        final int times;

        times = mc.toNumericValue(stack.getFirst()).intValue();
        stack = stack.getRest();
        RippleValue p = stack.getFirst();
        final RippleList rest = stack.getRest();

        Sink<Operator, RippleException> opSink = new Sink<Operator, RippleException>() {
            public void put(final Operator op) throws RippleException {
                solutions.put(arg.with(rest.push(
                        new StackMappingWrapper(new TimesQuantifier(op, times, times), mc))));
            }
        };

        Operator.createOperator(p, opSink, mc);
    }
}

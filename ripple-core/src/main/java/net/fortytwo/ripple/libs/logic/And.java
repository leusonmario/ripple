/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2011 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.logic;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.libs.stack.StackLibrary;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.StackMapping;

/**
 * A primitive which consumes two Boolean values and produces the result of
 * their logical conjunction.
 */
public class And extends PrimitiveStackMapping {
    private static final String[] IDENTIFIERS = {
            LogicLibrary.NS_2011_08 + "and",
            LogicLibrary.NS_2008_08 + "and",
            StackLibrary.NS_2007_08 + "and",
            StackLibrary.NS_2007_05 + "and"};

    private final StackMapping self = this;

    public And() throws RippleException {
        super();
    }

    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("x", "a boolean value (xsd:true or xsd:false)", true),
                new Parameter("y", "a boolean value (xsd:true or xsd:false)", true)};
    }

    public String getComment() {
        return "x y  =>  z  -- where z is true if x and y are true, otherwise false";
    }

    public String[] getIdentifiers() {
        return IDENTIFIERS;
    }

    public void apply(final RippleList arg,
                      final Sink<RippleList> solutions,
                      final ModelConnection mc) throws RippleException {

        RippleList stack = arg;

        boolean x, y;

        x = mc.toBoolean(stack.getFirst());
        stack = stack.getRest();
        y = mc.toBoolean(stack.getFirst());
        stack = stack.getRest();

        RippleValue result = mc.booleanValue(x && y);

        solutions.put(
                stack.push(result));
    }

    @Override
    public StackMapping getInverse() {
        return new StackMapping() {
            public int arity() {
                return 1;
            }

            public StackMapping getInverse() throws RippleException {
                return self;
            }

            public boolean isTransparent() {
                return true;
            }

            public void apply(final RippleList arg,
                              final Sink<RippleList> solutions,
                              final ModelConnection mc) throws RippleException {

                RippleList stack = arg;

                boolean x;

                x = mc.toBoolean(stack.getFirst());
                stack = stack.getRest();

                if (x) {
                    RippleValue t = mc.booleanValue(true);
                    solutions.put(stack.push(t).push(t));
                } else {
                    RippleValue t = mc.booleanValue(true);
                    RippleValue f = mc.booleanValue(false);
                    solutions.put(stack.push(t).push(f));
                    solutions.put(stack.push(f).push(t));
                    solutions.put(stack.push(f).push(f));
                }
            }
        };
    }
}


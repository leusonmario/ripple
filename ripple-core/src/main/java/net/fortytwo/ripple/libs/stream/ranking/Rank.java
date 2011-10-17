package net.fortytwo.ripple.libs.stream.ranking;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.libs.stream.StreamLibrary;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.StackContext;

/*
(("one" "two" 0.5 amp.)
 ("one" "two" 0.1 amp.)
 ("one" "hundred" 0.3 amp.)) each. apply. 0.1 amp. 10000 rank. .

("one" "two") each. apply. 10000 rank. .
(("one")) each. apply. 10000 rank. .
 */

/**
 * User: josh
 * Date: 4/4/11
 * Time: 1:48 PM
 */
public class Rank extends PrimitiveStackMapping {
    @Override
    public String[] getIdentifiers() {
        return new String[]{
                StreamLibrary.NS_2011_08 + "rank"
        };
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("steps", "the number of computational steps to perform before producing a ranking result", true)};
    }

    @Override
    public String getComment() {
        return "a closed-world ranking evaluator which aggregates weights of traversed paths.  Use in conjunction with etc:amp.";
    }

    public void apply(final StackContext arg,
                      final Sink<StackContext> solutions) throws RippleException {
        // FIXME: cheat to temporarily disable asynchronous query answering
        boolean a = Ripple.asynchronousQueries();
        Ripple.enableAsynchronousQueries(false);
        try {
            final ModelConnection mc = arg.getModelConnection();
            RippleList stack = arg.getStack();
            int steps = mc.toNumericValue(stack.getFirst()).intValue();
            stack = stack.getRest();

            //System.out.println("ranking on: " + stack);
            new RankingEvaluator(steps).apply(arg.with(stack), solutions);
        } finally {
            Ripple.enableAsynchronousQueries(a);
        }
    }
}

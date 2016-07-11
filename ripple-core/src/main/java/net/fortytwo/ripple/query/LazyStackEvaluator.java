package net.fortytwo.ripple.query;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.Closure;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.Operator;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.StackMapping;

// Note: not thread-safe, on account of stop()

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LazyStackEvaluator extends StackEvaluator {

    private boolean stopped = true;

    protected class EvaluatorSink implements Sink<RippleList> {
        private final Sink<RippleList> sink;
        private final ModelConnection mc;

        public EvaluatorSink(final Sink<RippleList> sink,
                             final ModelConnection mc) {
            this.sink = sink;
            this.mc = mc;
        }

        public void accept(final RippleList arg)
                throws RippleException {
            if (stopped || arg.isNil()) {
                return;
            }

            Object first = arg.getFirst();

            final StackMapping f = mc.toMapping(first);
            if (null != f) {
                RippleList rest = arg.getRest();

                // Nullary functions don't need their argument stacks reduced.
                // They shouldn't even care if the stack is empty.
                if (f.arity() == 0) {
                    f.apply(rest, this, mc);
                }

                // Functions with positive arity do require the stack to be
                // reduced, to one level per argument.
                else {
                    // We simply ignore stacks which can't be reduced to
                    // something with a passive item on top.
                    if (!rest.isNil()) {
                        final Sink<RippleList> thisEval = this;
                        Sink<RippleList> argSink = arg1 -> {
                            Closure c = new Closure(f, arg1.getFirst());
                            new EvaluatorSink(thisEval, mc).accept(arg1.getRest().push(new Operator(c)));
                        };

                        // Reduce the argument portion of the stack.
                        new EvaluatorSink(argSink, mc).accept(arg.getRest());
                    }
                }
            } else {
                sink.accept(arg);
            }
        }
    }

    public void apply(final RippleList arg,
                      final Sink<RippleList> solutions,
                      final ModelConnection mc) throws RippleException {
        if (arg.isNil()) {
            return;
        }

        EvaluatorSink evalSink = new EvaluatorSink(solutions, mc);
        stopped = false;

        try {
            evalSink.accept(arg);
        } catch (StackOverflowError e) {
            // Attempt to recover from stack overflow.
            throw new RippleException(e);
        }
    }

    public void stop() {
        synchronized (this) {
            stopped = true;
        }
    }
}


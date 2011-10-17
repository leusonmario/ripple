package net.fortytwo.ripple.libs.blueprints;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.StackContext;
import net.fortytwo.ripple.model.StackMapping;

/**
 * User: josh
 * Date: 4/5/11
 * Time: 8:11 PM
 */
public class Tail extends PrimitiveStackMapping {
    @Override
    public String[] getIdentifiers() {
        return new String[]{
                BlueprintsLibrary.NS_2011_08 + "tail"
        };
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{
                new Parameter("edge", "an edge, which has exactly one tail", true)};
    }

    @Override
    public String getComment() {
        return "finds the tail of an edge (the vertex from which the edge is outgoing)." +
                " The inverse mapping finds the outgoing edges of a vertex.";
    }

    @Override
    public void apply(final StackContext arg,
                      final Sink<StackContext> solutions) throws RippleException {
        RippleList stack = arg.getStack();
        RippleValue first = stack.getFirst();
        stack = stack.getRest();

        if (first instanceof EdgeValue) {
            Edge edge = ((EdgeValue) first).getElement();
            solutions.put(arg.with(stack.push(new VertexValue(edge.getOutVertex()))));
        }
    }

    @Override
    public StackMapping getInverse() {
        return inverseMapping;
    }

    private final StackMapping thisMapping = this;

    private final StackMapping inverseMapping = new StackMapping() {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public StackMapping getInverse() throws RippleException {
            return thisMapping;
        }

        @Override
        public boolean isTransparent() {
            return true;
        }

        @Override
        public void apply(final StackContext arg,
                          final Sink<StackContext> solutions) throws RippleException {
            RippleList stack = arg.getStack();
            RippleValue first = stack.getFirst();
            stack = stack.getRest();

            if (first instanceof VertexValue) {
                Vertex vertex = ((VertexValue) first).getElement();

                for (Edge edge : vertex.getOutEdges()) {
                    solutions.put(arg.with(stack.push(new EdgeValue(edge))));
                }
            }
        }
    };
}

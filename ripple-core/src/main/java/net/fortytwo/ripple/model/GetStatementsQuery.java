package net.fortytwo.ripple.model;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.flow.Buffer;
import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.sail.RippleSesameValue;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GetStatementsQuery {
    public class InvalidQueryException extends RippleException {
        public InvalidQueryException(final String message) {
            super(message);
        }
    }

    private enum Type {SP_O, PO_S, SO_P}

    // TODO: make this into a configuration property, or find another solution
    private static final boolean STRING_LITERALS_EQUIVALENT_TO_PLAIN_LITERALS = true;

    // TODO: use model-specific factory
    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Resource subject;
    private final IRI predicate;
    private final Value object;
    private final Resource[] contexts;
    private Type type = Type.SP_O;

    public GetStatementsQuery(final StatementPatternQuery patternQuery,
                              final ModelConnection mc) throws RippleException {
        try {
            switch (patternQuery.getPattern()) {
                case SP_O:
                    type = Type.SP_O;
                    subject = getResource(patternQuery.getSubject(), mc);
                    predicate = getURI(patternQuery.getPredicate(), mc);
                    object = null;
                    break;
                case PO_S:
                    type = Type.PO_S;
                    subject = null;
                    predicate = getURI(patternQuery.getPredicate(), mc);
                    object = getValue(patternQuery.getObject(), mc);
                    break;
                case SO_P:
                    type = Type.SO_P;
                    subject = getResource(patternQuery.getSubject(), mc);
                    predicate = null;
                    object = getValue(patternQuery.getObject(), mc);
                    break;
                default:
                    throw new InvalidQueryException("unsupported query pattern: " + patternQuery.getPattern());
            }

            Object[] rippleContexts = patternQuery.getContexts();
            this.contexts = new Resource[rippleContexts.length];

            for (int i = 0; i < rippleContexts.length; i++) {
                Resource context = getResource(rippleContexts[i], mc);

                // rdf:nil is a special case -- as a analysis name in Ripple, it
                // actually represents the null analysis.
                if (null != context && context.equals(RDF.NIL)) {
                    context = null;
                }

                this.contexts[i] = context;
            }
        } catch (ClassCastException e) {
            throw new InvalidQueryException("value could not be cast to the appropriate Sesame type");
        }
    }

    private IRI getURI(final Object rv, final ModelConnection mc) throws RippleException, ClassCastException {
        return (IRI) mc.toRDF(rv);
    }

    private Resource getResource(final Object rv, final ModelConnection mc)
            throws RippleException, ClassCastException {

        Value v = mc.toRDF(rv);
        return null == v ? null : (Resource) v;
    }

    private Value getValue(final Object rv, final ModelConnection mc) throws RippleException {
        return mc.toRDF(rv);
    }

    private void getStatements(final SailConnection sc, final Sink<Statement> results) throws RippleException {
        getStatementsPrivate(results, sc, subject, predicate, object);

        if (STRING_LITERALS_EQUIVALENT_TO_PLAIN_LITERALS
                && null != object
                && object instanceof Literal) {
            IRI datatype = ((Literal) object).getDatatype();
            if (null == datatype) {
                Literal newObj = valueFactory.createLiteral(((Literal) object).getLabel(), XMLSchema.STRING);
                getStatementsPrivate(results, sc, subject, predicate, newObj);
            } else if (XMLSchema.STRING == datatype) {
                Literal newObj = valueFactory.createLiteral(((Literal) object).getLabel());
                getStatementsPrivate(results, sc, subject, predicate, newObj);
            }
        }
    }

    private void getStatementsPrivate(final Sink<Statement> results,
                                      final SailConnection sc,
                                      Resource subject,
                                      IRI predicate,
                                      Value object) throws RippleException {
        if (null != object && object instanceof RippleSesameValue) {
            object = ((RippleSesameValue) object).getNativeValue();
        }

        // Note: we must collect results in a buffer before putting anything
        //       into the sink, as inefficient as that is, because otherwise
        //       we might end up opening another RepositoryResult before
        //       the one below closes, which currently causes Sesame to
        //       deadlock.  Even using a separate RepositoryConnection for
        //       each RepositoryResult doesn't seem to help.
        Buffer<Statement> buffer = new Buffer<>(results);
        CloseableIteration<? extends Statement, SailException> stmtIter;

//TODO: use CloseableIterationSource

        // Perform the query and collect results.
        try {
            stmtIter = sc.getStatements(subject, predicate, object, false, contexts);
            try {
                while (stmtIter.hasNext()) {
                    buffer.accept(stmtIter.next());
                }
            } finally {
                stmtIter.close();
            }
        } catch (SailException e) {
            throw new RippleException(e);
        }

        buffer.flush();
    }

    public void getValues(final SailConnection sc, final Sink<Value> results) throws RippleException {
        Sink<Statement> stSink = st -> {
            Value result;

            switch (type) {
                case SP_O:
                    result = st.getObject();
                    break;
                case PO_S:
                    result = st.getSubject();
                    break;
                case SO_P:
                    result = st.getPredicate();
                    break;
                default:
                    throw new RippleException("unhandled query type: " + type);
            }

            results.accept(result);
        };

        getStatements(sc, stSink);
    }
}

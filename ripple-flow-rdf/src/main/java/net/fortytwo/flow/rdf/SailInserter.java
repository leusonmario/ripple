package net.fortytwo.flow.rdf;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SailInserter implements RDFHandler {
    private final SailConnection sailConnection;

    public SailInserter(final SailConnection sailConnection) {
        this.sailConnection = sailConnection;
    }

    public void startRDF() throws RDFHandlerException {
    }

    public void endRDF() throws RDFHandlerException {
    }

    public void handleNamespace(final String prefix, final String uri) throws RDFHandlerException {
        try {
            sailConnection.setNamespace(prefix, uri);
        } catch (SailException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void handleStatement(final Statement st) throws RDFHandlerException {
        try {
            sailConnection.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
        } catch (SailException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void handleComment(final String comment) throws RDFHandlerException {
        // Do nothing.
    }
}

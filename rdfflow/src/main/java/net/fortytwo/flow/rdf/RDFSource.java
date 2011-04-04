/*
 * $URL: https://ripple.googlecode.com/svn/trunk/ripple-rdf/src/main/java/net/fortytwo/flow/rdf/RDFSource.java $
 * $Revision: 135 $
 * $Author: parcour $
 *
 * Copyright (C) 2007-2011 Joshua Shinavier
 */


package net.fortytwo.flow.rdf;

import net.fortytwo.flow.Source;

import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;

public abstract class RDFSource<E extends Exception>
{
	public abstract Source<Statement, E> statementSource() throws E;
	public abstract Source<Namespace, E> namespaceSource() throws E;
	public abstract Source<String, E> commentSource() throws E;

	public void writeTo( final RDFSink<E> sink ) throws E
	{
        try {
            commentSource().writeTo( sink.commentSink() );

            // Note: it's often important that namespaces are written before
            // statements.
            namespaceSource().writeTo( sink.namespaceSink() );

            statementSource().writeTo( sink.statementSink() );
        } catch (Exception e) {
            throw (E) e;
        }
	}
}

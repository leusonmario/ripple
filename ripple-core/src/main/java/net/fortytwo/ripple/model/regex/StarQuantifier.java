/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2011 Joshua Shinavier
 */


package net.fortytwo.ripple.model.regex;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.StackMapping;
import net.fortytwo.ripple.model.Operator;
import net.fortytwo.ripple.model.StackContext;
import net.fortytwo.ripple.model.NullStackMapping;

/**
 * Author: josh
 * Date: Feb 14, 2008
 * Time: 4:28:41 PM
 */
public class StarQuantifier implements StackMapping
{
	private final Operator innerOperator;

	public StarQuantifier( final Operator oper )
	{
		innerOperator = oper;
	}

	public int arity()
	{
		// TODO
		return 1;
	}

	public boolean isTransparent()
	{
		return innerOperator.getMapping().isTransparent();
	}

	public void apply( final StackContext arg,
						 final Sink<StackContext> sink ) throws RippleException
	{
		sink.put( arg );

		sink.put( arg.with( arg.getStack()
				.push( innerOperator )
				.push( new Operator( this ) ) ) );
	}

    public StackMapping getInverse() throws RippleException
    {
        return new NullStackMapping();
    }
}

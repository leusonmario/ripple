/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2012 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.stack;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.libs.control.Fold;
import net.fortytwo.ripple.libs.control.Map;
import net.fortytwo.ripple.model.Library;
import net.fortytwo.ripple.model.LibraryLoader;

/**
 * A collection of stack manipulation primitives.  Compare with Joy and other
 * functional stack languages.
 */
public class StackLibrary extends Library
{
    public static final String
            NS_2011_08 = "http://fortytwo.net/2011/08/ripple/stack#",
            NS_2008_08 = "http://fortytwo.net/2008/08/ripple/stack#",
            NS_2007_08 = "http://fortytwo.net/2007/08/ripple/stack#",
            NS_2007_05 = "http://fortytwo.net/2007/05/ripple/stack#";

    public void load(final LibraryLoader.Context context)
		throws RippleException
	{
		// Stack shuffling primitives
        registerPrimitives( context,
                Dup.class,
                Dupd.class,
                Pop.class,
                Popd.class,
                Rolldown.class,
                Rolldownd.class,
                Rollup.class,
                Rollupd.class,
                Rotate.class,
                Rotated.class,
                Self.class,
                Swap.class,
                Swapd.class,
                Top.class);

		// List primitives.
		registerPrimitives( context,
                At.class,
                Cat.class,
                Cons.class,
                Drop.class,
                Empty.class,
                Fold.class,
                Has.class,
                In.class,
                Map.class,
                Max.class,
                Min.class,
                Of.class,
                Size.class,
                Swons.class,
                Take.class,
                Uncons.class,
                Unswons.class );
	}
}


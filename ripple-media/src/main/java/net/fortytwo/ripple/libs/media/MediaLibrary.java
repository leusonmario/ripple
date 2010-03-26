/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2010 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.media;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.URIMap;
import net.fortytwo.ripple.model.Library;
import net.fortytwo.ripple.model.LibraryLoader;

public class MediaLibrary extends Library
{
     public static final String
            NS_2007_08 = "http://fortytwo.net/2007/08/ripple/media#";

    public void load( final URIMap uf,
                      final LibraryLoader.Context context )
		throws RippleException
	{
		uf.put(
			NS_2007_08, getClass().getResource( "media.ttl" ) + "#" );

		registerPrimitive( Play.class, context );
		registerPrimitive( Show.class, context );
		registerPrimitive( Speak.class, context );
	}
}

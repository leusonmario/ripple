/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2011 Joshua Shinavier
 */


package net.fortytwo.flow.diff;

import net.fortytwo.flow.Sink;

/**
 * Author: josh
 * Date: Mar 7, 2008
 * Time: 1:20:34 PM
 */
public interface DiffSink<T, E extends Exception>
{
    Sink<T, E> getPlus();
    Sink<T, E> getMinus();
}

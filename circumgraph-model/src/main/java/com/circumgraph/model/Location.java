package com.circumgraph.model;

import com.circumgraph.model.internal.LocationImpl;
import com.circumgraph.model.internal.MergedLocationImpl;

import org.eclipse.collections.api.factory.Lists;

/**
 * Representation of a location.
 */
@FunctionalInterface
public interface Location
{
	/**
	 * Turn this object into a developer-friendly message describing the
	 * location.
	 */
	String describe();

	/**
	 * Merge this location with another one. Used when things are modified
	 * dynamically.
	 *
	 * @param other
	 * @return
	 */
	default MergedLocation mergeWith(Location other)
	{
		return new MergedLocationImpl(Lists.immutable.of(this, other));
	}

	/**
	 * Create a location representing a position in the code. This will attempt
	 * to find the first non-model package and use it as the source.
	 *
	 * @return
	 *   instance representing the best known location
	 */
	static Location code()
	{
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for(int i=0; i<trace.length; i++)
		{
			String module = trace[i].getModuleName();
			String className = trace[i].getClassName();
			if(! "java.base".equals(module) && ! className.contains("com.circumgraph.model"))
			{
				return new LocationImpl(trace[i].toString());
			}
		}

		return LocationImpl.UNKNOWN;
	}

	/**
	 * Use the given source location is non-null or generate one via code.
	 *
	 * @param picked
	 *   location to maybe use, may be {@code null}
	 * @return
	 *   instance representing the best known location, never {@code null}
	 */
	static Location automatic(Location picked)
	{
		return picked == null ? code() : picked;
	}

	/**
	 * Create a location from the given message.
	 *
	 * @param message
	 * @return
	 */
	static Location create(String message)
	{
		return new LocationImpl(message);
	}

	/**
	 * Get an object representing an unknown location.
	 *
	 * @return
	 */
	static Location unknown()
	{
		return LocationImpl.UNKNOWN;
	}
}

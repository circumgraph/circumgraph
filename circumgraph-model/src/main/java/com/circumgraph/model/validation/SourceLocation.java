package com.circumgraph.model.validation;

import com.circumgraph.model.internal.BasicSourceLocation;

/**
 * Representation of a source location.
 */
public interface SourceLocation
{
	/**
	 * Turn this object into a developer-friendly message describing the
	 * location.
	 */
	@Override
	String toString();

	/**
	 * Create a location representing a position in the code. This will attempt
	 * to find the first non-model package and use it as the source.
	 *
	 * @return
	 *   instance representing the best known location
	 */
	static SourceLocation code()
	{
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for(int i=0; i<trace.length; i++)
		{
			String className = trace[i].getClassName();
			if(! className.contains("com.circumgraph.model"))
			{
				return new BasicSourceLocation(trace[i].toString());
			}
		}

		return BasicSourceLocation.UNKNOWN;
	}

	/**
	 * Use the given source location is non-null or generate one via code.
	 *
	 * @param picked
	 *   location to maybe use, may be {@code null}
	 * @return
	 *   instance representing the best known location, never {@code null}
	 */
	static SourceLocation automatic(SourceLocation picked)
	{
		return picked == null ? code() : picked;
	}

	/**
	 * Create a location from the given message.
	 *
	 * @param message
	 * @return
	 */
	static SourceLocation create(String message)
	{
		return new BasicSourceLocation(message);
	}

	/**
	 * Get an object representing an unknown location.
	 *
	 * @return
	 */
	static SourceLocation unknown()
	{
		return BasicSourceLocation.UNKNOWN;
	}
}

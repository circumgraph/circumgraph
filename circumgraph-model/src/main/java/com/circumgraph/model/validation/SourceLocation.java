package com.circumgraph.model.validation;

import com.circumgraph.model.internal.BasicSourceLocation;

/**
 * Representation of a source location.
 */
@FunctionalInterface
public interface SourceLocation
{
	/**
	 * Turn this object into a developer-friendly message describing the
	 * location.
	 */
	String describe();

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
			String module = trace[i].getModuleName();
			String className = trace[i].getClassName();
			if(! "java.base".equals(module) && ! className.contains("com.circumgraph.model"))
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

	/**
	 * Get a location representing a line in a given source.
	 *
	 * @param sourceName
	 * @param line
	 * @return
	 */
	static SourceLocation line(String sourceName, int line)
	{
		return () -> (sourceName == null ? "<source>" : sourceName)
			+ "@" + line;
	}

	/**
	 * Get a location representing a line and column in a given source.
	 *
	 * @param sourceName
	 * @param line
	 * @param column
	 * @return
	 */
	static SourceLocation line(String sourceName, int line, int column)
	{
		if(column < 0)
		{
			return line(sourceName, line);
		}

		return () -> (sourceName == null ? "<source>" : sourceName)
			+ "@" + line + ":" + column;
	}
}

package com.circumgraph.model;

import java.io.IOException;
import java.io.Reader;
import java.util.OptionalInt;

import com.circumgraph.model.internal.SourceLocationImpl;

import org.eclipse.collections.api.list.ListIterable;

import se.l4.ylem.io.IOSupplier;

/**
 * {@link Location} used for location in source code.
 */
public interface SourceLocation
	extends Location
{
	/**
	 * Get the name of the source file.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Get the line in the source file this location represents.
	 *
	 * @return
	 */
	OptionalInt getLine();

	/**
	 * Get the column of the {@link #getLine() line} in the source file this
	 * location represents.
	 *
	 * @return
	 */
	OptionalInt getColumn();

	/**
	 * Get a snippet of the source.
	 *
	 * @return
	 */
	ListIterable<Snippet> getSnippet()
		throws IOException;

	/**
	 * Get a location representing a line in a given source.
	 *
	 * @param sourceName
	 * @param line
	 * @return
	 */
	static SourceLocation forLine(String sourceName, int line)
	{
		return new SourceLocationImpl(sourceName, line, 0, null);
	}

	/**
	 * Get a location representing a line in a given source.
	 *
	 * @param sourceName
	 * @param line
	 * @param sourceTextSupplier
	 * @return
	 */
	static SourceLocation forLine(
		String sourceName,
		int line,
		IOSupplier<Reader> sourceTextSupplier
	)
	{
		return new SourceLocationImpl(sourceName, line, 0, sourceTextSupplier);
	}

	/**
	 * Get a location representing a line and column in a given source.
	 *
	 * @param sourceName
	 * @param line
	 * @param column
	 * @return
	 */
	static SourceLocation create(String sourceName, int line, int column)
	{
		return new SourceLocationImpl(sourceName, line, column, null);
	}

	/**
	 * Get a location representing a line and column in a given source.
	 *
	 * @param sourceName
	 * @param line
	 * @param column
	 * @param sourceTextSupplier
	 * @return
	 */
	static SourceLocation create(
		String sourceName,
		int line,
		int column,
		IOSupplier<Reader> sourceTextSupplier
	)
	{
		return new SourceLocationImpl(sourceName, line, column, sourceTextSupplier);
	}

	interface Snippet
	{
		/**
		 * Get the line number.
		 *
		 * @return
		 */
		int getLine();

		/**
		 * Get the text of the line.
		 *
		 * @return
		 */
		String getText();
	}
}

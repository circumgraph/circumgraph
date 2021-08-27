package com.circumgraph.schema.graphql;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.circumgraph.model.SourceLocation;

import se.l4.ylem.io.IOSupplier;

/**
 * Source of text being parsed into a {@link com.circumgraph.model.Schema}.
 */
public class TextSource
{
	private final String name;
	private final IOSupplier<Reader> sourceTextSupplier;

	public TextSource(String name, IOSupplier<Reader> readerSupplier)
	{
		this.name = name;
		this.sourceTextSupplier = readerSupplier;
	}

	/**
	 * Get the name of this source.
	 *
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Open the source.
	 *
	 * @return
	 */
	public Reader open()
		throws IOException
	{
		return sourceTextSupplier.get();
	}

	/**
	 * Create an instance of {@link SourceLocation} for a location in this
	 * source.
	 *
	 * @param line
	 * @param column
	 * @return
	 */
	public SourceLocation toLocation(int line, int column)
	{
		// TODO: Caching supplier
		return SourceLocation.create(name, line, column, sourceTextSupplier);
	}

	/**
	 * Create a source.
	 *
	 * @param sourceName
	 *   name of the source
	 * @param sourceTextSupplier
	 *   function for opening a reader
	 * @return
	 */
	public static TextSource create(String sourceName, IOSupplier<Reader> sourceTextSupplier)
	{
		return new TextSource(sourceName, sourceTextSupplier);
	}

	/**
	 * Create a source.
	 *
	 * @param sourceName
	 *   name of the source
	 * @param sourceText
	 *   source text
	 * @return
	 */
	public static TextSource create(String sourceName, String sourceText)
	{
		return new TextSource(sourceName, () -> new StringReader(sourceText));
	}
}

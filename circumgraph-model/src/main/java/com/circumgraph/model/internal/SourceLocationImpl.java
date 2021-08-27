package com.circumgraph.model.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.OptionalInt;

import com.circumgraph.model.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;

import se.l4.ylem.io.IOSupplier;

/**
 * Implementation of {@link SourceLocation}.
 */
public class SourceLocationImpl
	implements SourceLocation
{
	private final String name;
	private final int line;
	private final int column;
	private final IOSupplier<Reader> sourceTextSupplier;

	public SourceLocationImpl(
		String name,
		int line,
		int column,
		IOSupplier<Reader> sourceTextSupplier
	)
	{
		this.name = name == null || name.isEmpty() ? "<source>" : name;
		this.line = line;
		this.column = column;
		this.sourceTextSupplier = sourceTextSupplier;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public OptionalInt getLine()
	{
		return line <= 0 ? OptionalInt.empty() : OptionalInt.of(line);
	}

	@Override
	public OptionalInt getColumn()
	{
		return column <= 0 ? OptionalInt.empty() : OptionalInt.of(column);
	}

	@Override
	public ListIterable<Snippet> getSnippet()
		throws IOException
	{
		if(sourceTextSupplier == null || line <= 0)
		{
			return Lists.immutable.empty();
		}

		int minLine = Math.max(1, line - 1);
		int maxLine = line + 1;

		var result = Lists.mutable.<Snippet>empty();
		try(var reader = sourceTextSupplier.get(); var buffered = new BufferedReader(reader))
		{
			String line;
			int lineNumber = 1;
			while((line = buffered.readLine()) != null)
			{
				if(lineNumber > maxLine)
				{
					// Done reading our window
					break;
				}

				if(lineNumber >= minLine)
				{
					// In the window of lines considered interesting
					result.add(new SnippetImpl(lineNumber, line));
				}

				lineNumber++;
			}
		}

		return result;
	}

	@Override
	public String describe()
	{
		var builder = new StringBuilder();
		builder.append(this.name);
		if(line > 0)
		{
			builder.append(':').append(line);

			if(column > 0)
			{
				builder.append(':').append(column);
			}
		}

		return builder.toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(column, line, name);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		SourceLocationImpl other = (SourceLocationImpl) obj;
		return column == other.column
			&& line == other.line
			&& Objects.equals(name, other.name);
	}

	private static class SnippetImpl
		implements Snippet
	{
		private final int line;
		private final String text;

		public SnippetImpl(int line, String text)
		{
			this.line = line;
			this.text = text;
		}

		@Override
		public int getLine()
		{
			return line;
		}

		@Override
		public String getText()
		{
			return text;
		}

		@Override
		public String toString()
		{
			return "Snippet{line=" + line + ", text=" + text + "}";
		}
	}
}

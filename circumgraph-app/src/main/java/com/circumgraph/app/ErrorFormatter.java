package com.circumgraph.app;

import java.io.IOException;

import com.circumgraph.model.Location;
import com.circumgraph.model.MergedLocation;
import com.circumgraph.model.SourceLocation;
import com.circumgraph.model.validation.ValidationMessage;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.factory.Lists;

/**
 * Error formatter.
 */
public class ErrorFormatter
{
	private static final String TAB_REPLACEMENT = "  ";

	/**
	 * . Used to control printing of source for non-errors.
	 */
	private boolean verbose;

	/**
	 * Set if errors should be verbose. Setting this to {@code true} enables
	 * printing of source for messages that are non-errors.
	 *
	 * @param verbose
	 */
	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}

	/**
	 * Get if formatted messages are verbose.
	 *
	 * @return
	 */
	public boolean isVerbose()
	{
		return verbose;
	}

	/**
	 * Format a {@link ValidationMessage}.
	 *
	 * @param msg
	 * @return
	 */
	public String format(ValidationMessage msg)
	{
		var builder = new StringBuilder();
		try
		{
			format(msg, builder);
		}
		catch(IOException e)
		{
			throw new RuntimeException("IOException while writing to StringBuilder", e);
		}

		return builder.toString();
	}

	/**
	 * Format a {@link ValidationMessage} appending the result to the given
	 * {@link Appendable}.
	 *
	 * @param msg
	 * @param builder
	 * @throws IOException
	 */
	public void format(ValidationMessage msg, Appendable builder)
		throws IOException
	{
		switch(msg.getLevel())
		{
			case ERROR:
				builder.append("error");
				break;
			case WARNING:
				builder.append("warning");
				break;
			case INFO:
				builder.append("info");
				break;
			default:
				builder.append("unknown");
				break;
		}

		builder.append('[');
		builder.append(msg.getCode());
		builder.append("]: ");
		builder.append(msg.getMessage());
		builder.append("\n");

		var printers = Lists.mutable.<LocationPrinter>empty();
		var gutterWidth = 2;
		for(var location : MergedLocation.toList(msg.getLocation()))
		{
			LocationPrinter printer;
			if(location instanceof SourceLocation sourceLocation)
			{
				ListIterable<SourceLocation.Snippet> snippets;
				try
				{
					snippets = sourceLocation.getSnippet();
				}
				catch(IOException e)
				{
					// Ignore IO
					snippets = Lists.immutable.empty();
				}

				printer = new SourceLocationPrinter(sourceLocation, snippets);
			}
			else
			{
				printer = new NonSourceLocationPrinter(location);
			}

			gutterWidth = Math.max(gutterWidth, printer.getGutterWidth());
			printers.add(printer);
		}

		var firstLocation = true;
		for(var printer : printers)
		{
			if(firstLocation)
			{
				firstLocation = false;
			}
			else
			{
				builder.append("\n");
			}

			printer.print(gutterWidth, builder);
		}
	}

	private interface LocationPrinter
	{
		int getGutterWidth();

		void print(int gutterWidth, Appendable builder)
			throws IOException;
	}

	private static class SourceLocationPrinter
		implements LocationPrinter
	{
		private SourceLocation sourceLocation;
		private ListIterable<SourceLocation.Snippet> snippets;

		public SourceLocationPrinter(SourceLocation sourceLocation, ListIterable<SourceLocation.Snippet> snippets)
		{
			this.sourceLocation = sourceLocation;
			this.snippets = snippets;
		}

		@Override
		public int getGutterWidth()
		{
			return snippets.isEmpty() ? 0 : Math.max(2, (int) Math.log10(snippets.getLast().getLine()) + 1);
		}

		@Override
		public void print(int gutterWidth, Appendable builder)
			throws IOException
		{
			printSpacing(gutterWidth, builder);
			builder.append("--> ");
			builder.append(sourceLocation.describe());

			if(snippets.isEmpty()) return;

			var lineNumberSpacer = " ".repeat(gutterWidth);
			var line = sourceLocation.getLine().orElse(0);

			builder.append("\n");
			builder.append(lineNumberSpacer).append(" |\n");

			for(var snippet : snippets)
			{
				var lineAsString = String.valueOf(snippet.getLine());
				printSpacing(gutterWidth - lineAsString.length(), builder);
				builder.append(lineAsString);
				builder.append(" |  ");
				builder.append(snippet.getText().replace("\t", TAB_REPLACEMENT));
				builder.append("\n");

				var text = snippet.getText();
				if(snippet.getLine() == line && sourceLocation.getColumn().isPresent())
				{
					// Print a column indicator
					builder.append(lineNumberSpacer).append(" |  ");
					for(int i=0, n=sourceLocation.getColumn().getAsInt() - 1; i<n; i++)
					{
						if(text.charAt(i) == '\t')
						{
							builder.append(TAB_REPLACEMENT);
						}
						else
						{
							builder.append(' ');
						}
					}
					builder.append("^^^");
					builder.append("\n");
				}
			}

			builder.append(lineNumberSpacer).append(" |");
		}
	}

	private static class NonSourceLocationPrinter
		implements LocationPrinter
	{
		private final Location location;

		public NonSourceLocationPrinter(Location location)
		{
			this.location = location;
		}

		@Override
		public int getGutterWidth()
		{
			return 0;
		}

		@Override
		public void print(int gutterWidth, Appendable builder)
			throws IOException
		{
			printSpacing(gutterWidth, builder);
			builder.append("--> ");
			builder.append(location.describe());
		}
	}

	private static void printSpacing(int width, Appendable a)
		throws IOException
	{
		for(int i=0; i<width; i++)
		{
			a.append(' ');
		}
	}
}

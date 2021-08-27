package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.OptionalInt;

import com.circumgraph.model.internal.SourceLocationImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import se.l4.ylem.io.IOSupplier;

public class SourceLocationTest
{
	private IOSupplier<Reader> source(int startLine, String value)
	{
		var merged = "\n".repeat(startLine - 1) + value;
		return () -> new StringReader(merged);
	}

	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(SourceLocationImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceTextSupplier"
			)
			.verify();
	}

	@Test
	public void testForLine()
		throws IOException
	{
		var loc = SourceLocation.forLine("<source>", 1);
		var snippets = loc.getSnippet();

		assertThat(loc.getName(), is("<source>"));
		assertThat(loc.getLine(), is(OptionalInt.of(1)));
		assertThat(loc.describe(), is("<source>:1"));
		assertThat(snippets.isEmpty(), is(true));
	}

	@Test
	public void testForLineWithSourceSingleLine()
		throws IOException
	{
		var loc = SourceLocation.forLine("<source>", 1, source(1, "First line"));
		var snippets = loc.getSnippet();

		assertThat(loc.getName(), is("<source>"));
		assertThat(loc.getLine(), is(OptionalInt.of(1)));
		assertThat(loc.describe(), is("<source>:1"));

		assertThat(snippets.size(), is(1));

		var s1 = snippets.getFirst();
		assertThat(s1.getLine(), is(1));
		assertThat(s1.getText(), is("First line"));
	}

	@Test
	public void testForLineWithSourceLinePreceding()
		throws IOException
	{
		var loc = SourceLocation.forLine("<source>", 2, source(1, "First line\nSecond line"));
		var snippets = loc.getSnippet();

		assertThat(loc.getName(), is("<source>"));
		assertThat(loc.getLine(), is(OptionalInt.of(2)));
		assertThat(loc.describe(), is("<source>:2"));

		assertThat(snippets.size(), is(2));

		var s1 = snippets.get(0);
		assertThat(s1.getLine(), is(1));
		assertThat(s1.getText(), is("First line"));

		var s2 = snippets.get(1);
		assertThat(s2.getLine(), is(2));
		assertThat(s2.getText(), is("Second line"));
	}

	@Test
	public void testForLineWithSourceLinePrecedingSuceeding()
		throws IOException
	{
		var loc = SourceLocation.forLine("<source>", 2, source(1, "First line\nSecond line\nThird line"));
		var snippets = loc.getSnippet();

		assertThat(loc.getName(), is("<source>"));
		assertThat(loc.getLine(), is(OptionalInt.of(2)));
		assertThat(loc.describe(), is("<source>:2"));

		assertThat(snippets.size(), is(3));

		var s1 = snippets.get(0);
		assertThat(s1.getLine(), is(1));
		assertThat(s1.getText(), is("First line"));

		var s2 = snippets.get(1);
		assertThat(s2.getLine(), is(2));
		assertThat(s2.getText(), is("Second line"));

		var s3 = snippets.get(2);
		assertThat(s3.getLine(), is(3));
		assertThat(s3.getText(), is("Third line"));
	}

	@Test
	public void testForLineWithSourceLinePrecedingSuceeding2()
		throws IOException
	{
		var loc = SourceLocation.forLine("<source>", 2, source(1, "First line\nSecond line\nThird line\nFourth line"));
		var snippets = loc.getSnippet();

		assertThat(loc.getName(), is("<source>"));
		assertThat(loc.getLine(), is(OptionalInt.of(2)));
		assertThat(loc.describe(), is("<source>:2"));

		assertThat(snippets.size(), is(3));

		var s1 = snippets.get(0);
		assertThat(s1.getLine(), is(1));
		assertThat(s1.getText(), is("First line"));

		var s2 = snippets.get(1);
		assertThat(s2.getLine(), is(2));
		assertThat(s2.getText(), is("Second line"));

		var s3 = snippets.get(2);
		assertThat(s3.getLine(), is(3));
		assertThat(s3.getText(), is("Third line"));
	}
}

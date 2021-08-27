package com.circumgraph.app;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.Reader;
import java.io.StringReader;

import com.circumgraph.model.Location;
import com.circumgraph.model.SourceLocation;
import com.circumgraph.model.validation.ValidationMessageType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.l4.ylem.io.IOSupplier;

public class ErrorFormatterTest
{
	private ErrorFormatter formatter;

	@BeforeEach
	public void setup()
	{
		formatter = new ErrorFormatter();
	}

	private IOSupplier<Reader> source(int startLine, String value)
	{
		var merged = "\n".repeat(startLine - 1) + value;
		return () -> new StringReader(merged);
	}

	@Test
	public void testFormatInfo()
	{
		var text = formatter.format(ValidationMessageType.info()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(Location.unknown())
			.build()
		);

		assertThat(text, is("info[test-code]: Message\n  --> Unknown Location"));
	}

	@Test
	public void testFormatWarning()
	{
		var text = formatter.format(ValidationMessageType.warn()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(Location.unknown())
			.build()
		);

		assertThat(text, is("warning[test-code]: Message\n  --> Unknown Location"));
	}

	@Test
	public void testFormatError()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(Location.unknown())
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> Unknown Location"));
	}

	@Test
	public void testFormatMergedNoSource()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(Location.create("LOC1").mergeWith(Location.create("LOC2")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> LOC1\n  --> LOC2"));
	}

	@Test
	public void testFormatSourceSingleLine()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(SourceLocation.forLine("<source>", 1, source(1, "Hello World")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> <source>:1\n   |\n 1 |  Hello World\n   |"));
	}

	@Test
	public void testFormatSourceMultipleLinesOnlySuceeding()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(SourceLocation.forLine("<source>", 1, source(1, "Hello\nWorld")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> <source>:1\n   |\n 1 |  Hello\n 2 |  World\n   |"));
	}

	@Test
	public void testFormatSourceMultipleLinesPreceedingAndSuceeding()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(SourceLocation.forLine("<source>", 2, source(1, "Hello\nWorld\nStuff")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> <source>:2\n   |\n 1 |  Hello\n 2 |  World\n 3 |  Stuff\n   |"));
	}

	@Test
	public void testFormatSourceLineColumn()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(SourceLocation.create("<source>", 1, 7, source(1, "Hello World")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> <source>:1:7\n   |\n 1 |  Hello World\n   |        ^^^\n   |"));
	}

	@Test
	public void testFormatSourceLineColumnPreceedingAndSuceeding()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(SourceLocation.create("<source>", 2, 1, source(1, "Hello\nWorld\nStuff")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> <source>:2:1\n   |\n 1 |  Hello\n 2 |  World\n   |  ^^^\n 3 |  Stuff\n   |"));
	}

	@Test
	public void testFormatSourceTab()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(SourceLocation.forLine("<source>", 1, source(1, "Hello\tWorld")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> <source>:1\n   |\n 1 |  Hello  World\n   |"));
	}

	@Test
	public void testFormatSourceTabWithColumn()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(SourceLocation.create("<source>", 2, 2, source(1, "Hello\n\tWorld\nStuff")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> <source>:2:2\n   |\n 1 |  Hello\n 2 |    World\n   |    ^^^\n 3 |  Stuff\n   |"));
	}

	@Test
	public void testFormatSourceLineColumnStartingAtLine9()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(SourceLocation.create("<source>", 10, 1, source(9, "Hello\nWorld\nStuff")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n  --> <source>:10:1\n   |\n 9 |  Hello\n10 |  World\n   |  ^^^\n11 |  Stuff\n   |"));
	}

	@Test
	public void testFormatSourceLineColumnStartingAtLine99()
	{
		var text = formatter.format(ValidationMessageType.error()
			.withCode("test-code")
			.withMessage("Message")
			.build()
			.toMessage()
			.withLocation(SourceLocation.create("<source>", 100, 1, source(99, "Hello\nWorld\nStuff")))
			.build()
		);

		assertThat(text, is("error[test-code]: Message\n   --> <source>:100:1\n    |\n 99 |  Hello\n100 |  World\n    |  ^^^\n101 |  Stuff\n    |"));
	}
}

package com.circumgraph.storage.types.strings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.ScalarConversionException;
import com.circumgraph.storage.scalars.StringScalar;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class StringScalarTest
	extends ScalarTest
{
	@Test
	public void testToGraphQL()
		throws IOException
	{
		var instance = new StringScalar();
		var converted = instance.toGraphQL("value");
		assertThat(converted, is("value"));
	}

	@Test
	public void testToJava()
		throws IOException
	{
		var instance = new StringScalar();
		var converted = instance.toJava("value");
		assertThat(converted, is("value"));
	}

	@Test
	public void testToJavaInvalidNull()
		throws IOException
	{
		var instance = new StringScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(null));
	}

	@Test
	public void testRead()
		throws IOException
	{
		var instance = new StringScalar();

		var in = write(out -> {
			out.writeString("Hello world");
		});

		var value = instance.read(in);
		assertThat(value, is("Hello world"));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new StringScalar();

		var in = write(out -> {
			instance.write(
				"Hello world",
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("Hello world"));
	}
}

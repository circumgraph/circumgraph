package com.circumgraph.storage.types.ints;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.IntScalar;
import com.circumgraph.storage.scalars.ScalarConversionException;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class IntScalarTest
	extends ScalarTest
{
	@Test
	public void testToGraphQL()
		throws IOException
	{
		var instance = new IntScalar();
		var converted = instance.toGraphQL(100);
		assertThat(converted, is(100));
	}

	@Test
	public void testToJava()
		throws IOException
	{
		var instance = new IntScalar();
		var converted = instance.toJava(100);
		assertThat(converted, is(100));
	}

	@Test
	public void testToJavaString()
		throws IOException
	{
		var instance = new IntScalar();
		var converted = instance.toJava("100");
		assertThat(converted, is(100));
	}

	@Test
	public void testToJavaNumericButNotInteger()
		throws IOException
	{
		var instance = new IntScalar();
		var converted = instance.toJava(100.0);
		assertThat(converted, is(100));
	}

	@Test
	public void testToJavaInvalidStringFormat()
		throws IOException
	{
		var instance = new IntScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava("invalid"));
	}

	@Test
	public void testToJavaInvalidNull()
		throws IOException
	{
		var instance = new IntScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(null));
	}

	@Test
	public void testRead()
		throws IOException
	{
		var instance = new IntScalar();

		var in = write(out -> {
			out.writeInt(100);
		});

		var value = instance.read(in);
		assertThat(value, is(100));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new IntScalar();

		var in = write(out -> {
			instance.write(
				100,
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(100));
	}
}

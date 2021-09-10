package com.circumgraph.storage.types.floats;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.FloatScalar;
import com.circumgraph.storage.scalars.ScalarConversionException;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class FloatScalarTest
	extends ScalarTest
{
	@Test
	public void testToGraphQL()
		throws IOException
	{
		var instance = new FloatScalar();
		var converted = instance.toGraphQL(100.2);
		assertThat(converted, is(100.2));
	}

	@Test
	public void testToJava()
		throws IOException
	{
		var instance = new FloatScalar();
		var converted = instance.toJava(100.2);
		assertThat(converted, is(100.2));
	}

	@Test
	public void testToJavaString()
		throws IOException
	{
		var instance = new FloatScalar();
		var converted = instance.toJava("100.2");
		assertThat(converted, is(100.2));
	}

	@Test
	public void testToJavaStringNoDecimals()
		throws IOException
	{
		var instance = new FloatScalar();
		var converted = instance.toJava("100");
		assertThat(converted, is(100.0));
	}

	@Test
	public void testToJavaNumericButNotDouble()
		throws IOException
	{
		var instance = new FloatScalar();
		var converted = instance.toJava(100);
		assertThat(converted, is(100.0));
	}

	@Test
	public void testToJavaInvalidStringFormat()
		throws IOException
	{
		var instance = new FloatScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava("invalid"));
	}

	@Test
	public void testToJavaInvalidNull()
		throws IOException
	{
		var instance = new FloatScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(null));
	}

	@Test
	public void testRead()
		throws IOException
	{
		var instance = new FloatScalar();

		var in = write(out -> {
			out.writeDouble(123.45);
		});

		var value = instance.read(in);
		assertThat(value, is(123.45));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new FloatScalar();

		var in = write(out -> {
			instance.write(
				123.45,
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readDouble(), is(123.45));
	}
}

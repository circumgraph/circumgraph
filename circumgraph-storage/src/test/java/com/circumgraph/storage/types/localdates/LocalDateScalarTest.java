package com.circumgraph.storage.types.localdates;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.LocalDate;

import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.LocalDateScalar;
import com.circumgraph.storage.scalars.ScalarConversionException;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class LocalDateScalarTest
	extends ScalarTest
{
	@Test
	public void testToGraphQL()
	{
		var instance = new LocalDateScalar();
		var converted = instance.toGraphQL(LocalDate.of(2020, 1, 1));

		assertThat(converted, is("2020-01-01"));
	}

	@Test
	public void testToJava()
	{
		var instance = new LocalDateScalar();
		var parsed = instance.toJava("2020-01-01");

		assertThat(parsed, is(LocalDate.of(2020, 1, 1)));
	}

	@Test
	public void testToJavaInvalidStringFormat()
		throws IOException
	{
		var instance = new LocalDateScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava("invalid"));
	}

	@Test
	public void testToJavaInvalidNull()
		throws IOException
	{
		var instance = new LocalDateScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(null));
	}

	@Test
	public void testRead()
		throws IOException
	{
		var instance = new LocalDateScalar();

		var in = write(out -> {
			out.writeLong(200);
		});

		var value = instance.read(in);
		assertThat(value, is(LocalDate.ofEpochDay(200)));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new LocalDateScalar();

		var in = write(out -> {
			instance.write(
				LocalDate.ofEpochDay(200),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(200l));
	}
}

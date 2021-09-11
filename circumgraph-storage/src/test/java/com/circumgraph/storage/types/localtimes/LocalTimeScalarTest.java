package com.circumgraph.storage.types.localtimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.LocalTime;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.LocalTimeScalar;
import com.circumgraph.storage.scalars.ScalarConversionException;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class LocalTimeScalarTest
	extends ScalarTest
{
	@Test
	public void testTypes()
	{
		var instance = new LocalTimeScalar();
		assertThat(instance.getModelType(), is(ScalarDef.LOCAL_TIME));
		assertThat(instance.getGraphQLType(), is(String.class));
		assertThat(instance.getJavaType(), is(LocalTime.class));
	}

	@Test
	public void testToGraphQL()
	{
		var instance = new LocalTimeScalar();
		var converted = instance.toGraphQL(LocalTime.of(8, 50, 22));
		assertThat(converted, is("08:50:22"));
	}

	@Test
	public void testToGraphQLWithNanos()
	{
		var instance = new LocalTimeScalar();
		var converted = instance.toGraphQL(LocalTime.of(8, 50, 22, 999000000));
		assertThat(converted, is("08:50:22.999"));
	}

	@Test
	public void testToJava()
	{
		var instance = new LocalTimeScalar();
		var converted = instance.toJava("08:50:22");
		assertThat(converted, is(LocalTime.of(8, 50, 22)));
	}

	@Test
	public void testToJavaWithNanos()
	{
		var instance = new LocalTimeScalar();
		var converted = instance.toJava("08:50:22.999");
		assertThat(converted, is(LocalTime.of(8, 50, 22, 999000000)));
	}

	@Test
	public void testToJavaInvalidFormat()
	{
		var instance = new LocalTimeScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava("invalid"));
	}

	@Test
	public void testToJavaInvalidType()
	{
		var instance = new LocalTimeScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(82));
	}

	@Test
	public void testRead()
		throws IOException
	{
		var instance = new LocalTimeScalar();

		var in = write(out -> {
			out.writeLong(999000000);
		});

		var value = instance.read(in);
		assertThat(value, is(LocalTime.ofNanoOfDay(999000000)));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new LocalTimeScalar();

		var in = write(out -> {
			instance.write(
				LocalTime.ofNanoOfDay(999000000),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(999000000l));
	}

	@Test
	public void testWriteMidnight()
		throws IOException
	{
		var instance = new LocalTimeScalar();

		var in = write(out -> {
			instance.write(
				LocalTime.MIDNIGHT,
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(0l));
	}

	@Test
	public void testWriteBeforeMidnight()
		throws IOException
	{
		var instance = new LocalTimeScalar();

		var in = write(out -> {
			instance.write(
				LocalTime.MAX,
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(86399999999999l));
	}
}

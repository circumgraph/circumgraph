package com.circumgraph.storage.types.localdatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.LocalDateTime;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.LocalDateTimeScalar;
import com.circumgraph.storage.scalars.ScalarConversionException;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class LocalDateTimeScalarTest
	extends ScalarTest
{
	@Test
	public void testTypes()
	{
		var instance = new LocalDateTimeScalar();
		assertThat(instance.getModelType(), is(ScalarDef.LOCAL_DATE_TIME));
		assertThat(instance.getGraphQLType(), is(String.class));
		assertThat(instance.getJavaType(), is(LocalDateTime.class));
	}

	@Test
	public void testToGraphQL()
	{
		var instance = new LocalDateTimeScalar();
		var converted = instance.toGraphQL(
			LocalDateTime.of(2020, 1, 1, 8, 45, 30, 100000000)
		);

		assertThat(converted, is("2020-01-01T08:45:30.1"));
	}

	@Test
	public void testToJava()
	{
		var instance = new LocalDateTimeScalar();
		var converted = instance.toJava("2020-01-01T08:45:30.1");

		assertThat(converted, is(
			LocalDateTime.of(2020, 1, 1, 8, 45, 30, 100000000)
		));
	}

	@Test
	public void testToJavaInvalidStringFormat()
		throws IOException
	{
		var instance = new LocalDateTimeScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava("invalid"));
	}

	@Test
	public void testToJavaInvalidNull()
		throws IOException
	{
		var instance = new LocalDateTimeScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(null));
	}

	@Test
	public void testRead()
		throws IOException
	{
		var instance = new LocalDateTimeScalar();

		var in = write(out -> {
			out.writeLong(1630562280100l);
		});

		var value = instance.read(in);
		assertThat(value, is(LocalDateTime.of(2021, 9, 2, 5, 58, 0, 100000000)));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new LocalDateTimeScalar();

		var in = write(out -> {
			instance.write(
				LocalDateTime.of(2021, 9, 2, 5, 58, 0, 100000000),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));
	}
}

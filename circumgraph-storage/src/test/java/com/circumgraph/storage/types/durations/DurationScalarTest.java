package com.circumgraph.storage.types.durations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.Duration;
import java.time.Period;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.ScalarTest;
import com.circumgraph.storage.scalars.DurationScalar;
import com.circumgraph.storage.scalars.ScalarConversionException;

import org.junit.jupiter.api.Test;
import org.threeten.extra.PeriodDuration;

import se.l4.exobytes.streaming.Token;

public class DurationScalarTest
	extends ScalarTest
{
	@Test
	public void testTypes()
	{
		var instance = new DurationScalar();
		assertThat(instance.getModelType(), is(ScalarDef.DURATION));
		assertThat(instance.getGraphQLType(), is(String.class));
		assertThat(instance.getJavaType(), is(PeriodDuration.class));
	}

	@Test
	public void testToGraphQL()
	{
		var instance = new DurationScalar();
		var converted = instance.toGraphQL(PeriodDuration.of(Period.ofYears(1)));

		assertThat(converted, is("P1Y"));
	}

	@Test
	public void testToJava()
	{
		var instance = new DurationScalar();
		var parsed = instance.toJava("P1Y");

		assertThat(parsed, is(PeriodDuration.of(Period.ofYears(1))));
	}

	@Test
	public void testToJavaInvalidStringFormat()
		throws IOException
	{
		var instance = new DurationScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava("invalid"));
	}

	@Test
	public void testToJavaInvalidNull()
		throws IOException
	{
		var instance = new DurationScalar();
		assertThrows(ScalarConversionException.class, () -> instance.toJava(null));
	}


	@Test
	public void testReadZero()
		throws IOException
	{
		var instance = new DurationScalar();

		var in = write(out -> {
			out.writeString("PT0S");
		});

		var value = instance.read(in);
		assertThat(value, is(
			PeriodDuration.of(Duration.ZERO)
		));
	}

	@Test
	public void testReadOneYear()
		throws IOException
	{
		var instance = new DurationScalar();

		var in = write(out -> {
			out.writeString("P1Y");
		});

		var value = instance.read(in);
		assertThat(value, is(
			PeriodDuration.of(Period.ofYears(1))
		));
	}

	@Test
	public void testWriteZero()
		throws IOException
	{
		var instance = new DurationScalar();

		var in = write(out -> {
			instance.write(
				PeriodDuration.of(Duration.ofMillis(0)),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("PT0S"));
	}

	@Test
	public void testWriteOneYear()
		throws IOException
	{
		var instance = new DurationScalar();

		var in = write(out -> {
			instance.write(
				PeriodDuration.of(Period.ofYears(1)),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("P1Y"));
	}
}

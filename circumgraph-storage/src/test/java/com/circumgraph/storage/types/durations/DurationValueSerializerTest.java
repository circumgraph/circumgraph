package com.circumgraph.storage.types.durations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.Duration;
import java.time.Period;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.internal.serializers.DurationValueSerializer;
import com.circumgraph.storage.internal.serializers.ValueSerializerTest;

import org.junit.jupiter.api.Test;
import org.threeten.extra.PeriodDuration;

import se.l4.exobytes.streaming.Token;

public class DurationValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testReadZero()
		throws IOException
	{
		var instance = new DurationValueSerializer();

		var in = write(out -> {
			out.writeString("PT0S");
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(
			ScalarDef.DURATION,
			PeriodDuration.of(Duration.ZERO)
		)));
	}

	@Test
	public void testReadOneYear()
		throws IOException
	{
		var instance = new DurationValueSerializer();

		var in = write(out -> {
			out.writeString("P1Y");
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(
			ScalarDef.DURATION,
			PeriodDuration.of(Period.ofYears(1))
		)));
	}

	@Test
	public void testWriteZero()
		throws IOException
	{
		var instance = new DurationValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(
					ScalarDef.DURATION,
					PeriodDuration.of(Duration.ofMillis(0))
				),
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
		var instance = new DurationValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(
					ScalarDef.DURATION,
					PeriodDuration.of(Period.ofYears(1))
				),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("P1Y"));
	}
}

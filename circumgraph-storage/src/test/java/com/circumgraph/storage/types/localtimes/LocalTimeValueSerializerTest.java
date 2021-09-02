package com.circumgraph.storage.types.localtimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.LocalTime;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.internal.serializers.LocalTimeValueSerializer;
import com.circumgraph.storage.internal.serializers.ValueSerializerTest;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class LocalTimeValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testRead()
		throws IOException
	{
		var instance = new LocalTimeValueSerializer();

		var in = write(out -> {
			out.writeLong(999000000);
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(ScalarDef.LOCAL_TIME, LocalTime.ofNanoOfDay(999000000))));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new LocalTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(ScalarDef.LOCAL_TIME, LocalTime.ofNanoOfDay(999000000)),
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
		var instance = new LocalTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(ScalarDef.LOCAL_TIME, LocalTime.MIDNIGHT),
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
		var instance = new LocalTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(ScalarDef.LOCAL_TIME, LocalTime.MAX),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(86399999999999l));
	}
}

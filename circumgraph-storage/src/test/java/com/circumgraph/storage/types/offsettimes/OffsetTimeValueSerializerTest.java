package com.circumgraph.storage.types.offsettimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.internal.serializers.OffsetTimeValueSerializer;
import com.circumgraph.storage.internal.serializers.ValueSerializerTest;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class OffsetTimeValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testReadUTC()
		throws IOException
	{
		var instance = new OffsetTimeValueSerializer();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(999000000l);
			out.writeInt(0);
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(
			ScalarDef.OFFSET_TIME,
			LocalTime.ofNanoOfDay(999000000).atOffset(ZoneOffset.UTC)
		)));
	}

	@Test
	public void testReadOneHourOffset()
		throws IOException
	{
		var instance = new OffsetTimeValueSerializer();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(999000000l);
			out.writeInt(3600);
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(
			ScalarDef.OFFSET_TIME,
			LocalTime.ofNanoOfDay(999000000).atOffset(ZoneOffset.ofHours(1))
		)));
	}

	@Test
	public void testWriteUTC()
		throws IOException
	{
		var instance = new OffsetTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(
					ScalarDef.OFFSET_TIME,
					OffsetTime.of(1, 0, 0, 0, ZoneOffset.UTC)
				),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(3600000000000L));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(0));

		assertThat(in.next(), is(Token.LIST_END));
	}

	@Test
	public void testWriteOneHourOffset()
		throws IOException
	{
		var instance = new OffsetTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(
					ScalarDef.OFFSET_TIME,
					OffsetTime.of(1, 0, 0, 0, ZoneOffset.ofHours(1))
				),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(3600000000000L));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(3600));

		assertThat(in.next(), is(Token.LIST_END));
	}
}

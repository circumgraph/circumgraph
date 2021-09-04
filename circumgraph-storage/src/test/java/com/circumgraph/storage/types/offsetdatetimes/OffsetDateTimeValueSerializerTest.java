package com.circumgraph.storage.types.offsetdatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.internal.serializers.OffsetDateTimeValueSerializer;
import com.circumgraph.storage.internal.serializers.ValueSerializerTest;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class OffsetDateTimeValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testReadUTC()
		throws IOException
	{
		var instance = new OffsetDateTimeValueSerializer();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(1630562280100l);
			out.writeInt(0);
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(
			ScalarDef.OFFSET_DATE_TIME,
			OffsetDateTime.of(2021, 9, 2, 5, 58, 0, 100000000, ZoneOffset.UTC)
		)));
	}

	@Test
	public void testReadOneHourOffset()
		throws IOException
	{
		var instance = new OffsetDateTimeValueSerializer();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(1630562280100l);
			out.writeInt(3600);
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(
			ScalarDef.OFFSET_DATE_TIME,
			OffsetDateTime.of(2021, 9, 2, 6, 58, 0, 100000000, ZoneOffset.ofHours(1))
		)));
	}

	@Test
	public void testWriteUTC()
		throws IOException
	{
		var instance = new OffsetDateTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(
					ScalarDef.OFFSET_DATE_TIME,
					OffsetDateTime.of(2021, 9, 2, 5, 58, 0, 100000000, ZoneOffset.UTC)
				),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(0));

		assertThat(in.next(), is(Token.LIST_END));
	}

	@Test
	public void testWriteOneHourOffset()
		throws IOException
	{
		var instance = new OffsetDateTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(
					ScalarDef.OFFSET_DATE_TIME,
					OffsetDateTime.of(2021, 9, 2, 6, 58, 0, 100000000, ZoneOffset.ofHours(1))
				),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(3600));

		assertThat(in.next(), is(Token.LIST_END));
	}
}

package com.circumgraph.storage.types.zoneddatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.internal.serializers.ValueSerializerTest;
import com.circumgraph.storage.internal.serializers.ZonedDateTimeValueSerializer;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class ZonedDateTimeValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testReadUTC()
		throws IOException
	{
		var instance = new ZonedDateTimeValueSerializer();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(1630562280100l);
			out.writeString("Z");
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(
			ScalarDef.ZONED_DATE_TIME,
			LocalDateTime.of(2021, 9, 2, 5, 58, 0, 100000000).atZone(ZoneOffset.UTC)
		)));
	}

	@Test
	public void testReadEuropeStockholm()
		throws IOException
	{
		var instance = new ZonedDateTimeValueSerializer();

		var in = write(out -> {
			out.writeListStart(2);
			out.writeLong(1630562280100l);
			out.writeString("Europe/Stockholm");
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(
			ScalarDef.ZONED_DATE_TIME,
			LocalDateTime.of(2021, 9, 2, 7, 58, 0, 100000000).atZone(ZoneId.of("Europe/Stockholm"))
		)));
	}

	@Test
	public void testWriteUTC()
		throws IOException
	{
		var instance = new ZonedDateTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(
					ScalarDef.ZONED_DATE_TIME,
					LocalDateTime.of(2021, 9, 2, 5, 58, 0, 100000000).atZone(ZoneOffset.UTC)
				),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("Z"));

		assertThat(in.next(), is(Token.LIST_END));
	}

	@Test
	public void testWriteEuropeStockholm()
		throws IOException
	{
		var instance = new ZonedDateTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(
					ScalarDef.ZONED_DATE_TIME,
					LocalDateTime.of(2021, 9, 2, 7, 58, 0, 100000000).atZone(ZoneId.of("Europe/Stockholm"))
				),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("Europe/Stockholm"));

		assertThat(in.next(), is(Token.LIST_END));
	}
}

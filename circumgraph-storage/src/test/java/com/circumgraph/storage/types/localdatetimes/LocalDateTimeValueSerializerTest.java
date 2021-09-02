package com.circumgraph.storage.types.localdatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.LocalDateTime;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.internal.serializers.LocalDateTimeValueSerializer;
import com.circumgraph.storage.internal.serializers.ValueSerializerTest;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class LocalDateTimeValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testRead()
		throws IOException
	{
		var instance = new LocalDateTimeValueSerializer();

		var in = write(out -> {
			out.writeLong(1630562280100l);
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(
			ScalarDef.LOCAL_DATE_TIME,
			LocalDateTime.of(2021, 9, 2, 5, 58, 0, 100000000)
		)));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new LocalDateTimeValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(
					ScalarDef.LOCAL_DATE_TIME,
					LocalDateTime.of(2021, 9, 2, 5, 58, 0, 100000000)
				),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(1630562280100l));
	}
}

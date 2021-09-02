package com.circumgraph.storage.types.localdates;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.LocalDate;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.internal.serializers.LocalDateValueSerializer;
import com.circumgraph.storage.internal.serializers.ValueSerializerTest;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class LocalDateValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testRead()
		throws IOException
	{
		var instance = new LocalDateValueSerializer();

		var in = write(out -> {
			out.writeLong(200);
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(ScalarDef.LOCAL_DATE, LocalDate.ofEpochDay(200))));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new LocalDateValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(ScalarDef.LOCAL_DATE, LocalDate.ofEpochDay(200)),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(200l));
	}
}

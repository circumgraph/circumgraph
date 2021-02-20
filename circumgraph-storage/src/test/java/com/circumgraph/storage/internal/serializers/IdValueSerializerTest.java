package com.circumgraph.storage.internal.serializers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.values.SimpleValue;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class IdValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testRead()
		throws IOException
	{
		var instance = new IdValueSerializer();

		var in = write(out -> {
			out.writeLong(100);
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.create(ScalarDef.ID, 100l)));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new IdValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.create(ScalarDef.ID, 100l),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readLong(), is(100l));
	}
}

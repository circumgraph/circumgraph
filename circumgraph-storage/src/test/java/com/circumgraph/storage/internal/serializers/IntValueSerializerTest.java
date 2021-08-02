package com.circumgraph.storage.internal.serializers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import com.circumgraph.storage.SimpleValue;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class IntValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testRead()
		throws IOException
	{
		var instance = new IntValueSerializer();

		var in = write(out -> {
			out.writeInt(100);
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.createInt(100)));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new IntValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.createInt(100),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readInt(), is(100));
	}
}

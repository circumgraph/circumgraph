package com.circumgraph.storage.internal.serializers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import com.circumgraph.values.SimpleValue;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class BooleanValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testRead()
		throws IOException
	{
		var instance = new BooleanValueSerializer();

		var in = write(out -> {
			out.writeBoolean(true);
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.createBoolean(true)));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new BooleanValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.createBoolean(true),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readBoolean(), is(true));
	}
}

package com.circumgraph.storage.internal.serializers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import com.circumgraph.values.SimpleValue;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class FloatValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testRead()
		throws IOException
	{
		var instance = new FloatValueSerializer();

		var in = write(out -> {
			out.writeDouble(123.45);
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.createFloat(123.45)));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new FloatValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.createFloat(123.45),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readDouble(), is(123.45));
	}
}

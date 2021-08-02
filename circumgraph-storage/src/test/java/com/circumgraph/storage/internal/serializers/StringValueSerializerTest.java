package com.circumgraph.storage.internal.serializers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import com.circumgraph.storage.SimpleValue;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class StringValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testRead()
		throws IOException
	{
		var instance = new StringValueSerializer();

		var in = write(out -> {
			out.writeString("Hello world");
		});

		var value = instance.read(in);
		assertThat(value, is(SimpleValue.createString("Hello world")));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var instance = new StringValueSerializer();

		var in = write(out -> {
			instance.write(
				SimpleValue.createString("Hello world"),
				out
			);
		});

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("Hello world"));
	}
}

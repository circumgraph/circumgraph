package com.circumgraph.storage.internal.serializers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.values.ListValue;
import com.circumgraph.values.SimpleValue;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class ListValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testRead()
		throws IOException
	{
		var def = ListDef.output(ScalarDef.STRING);

		var instance = new ListValueSerializer<>(
			def,
			new StringValueSerializer()
		);

		var in = write(out -> {
			out.writeListStart();
			out.writeString("a");
			out.writeString("b");
			out.writeListEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(
			ListValue.create(def, Lists.immutable.of(
				SimpleValue.createString("a"),
				SimpleValue.createString("b")
			))
		));
	}

	@Test
	public void testWrite()
		throws IOException
	{
		var def = ListDef.output(ScalarDef.STRING);

		var instance = new ListValueSerializer<>(
			def,
			new StringValueSerializer()
		);

		var in = write(out -> {
			instance.write(
				ListValue.create(def, Lists.immutable.of(
					SimpleValue.createString("a"),
					SimpleValue.createString("b")
				)),
				out
			);
		});

		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("a"));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("b"));

		assertThat(in.next(), is(Token.LIST_END));
	}
}

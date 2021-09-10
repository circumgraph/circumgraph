package com.circumgraph.storage.internal.serializers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.scalars.StringScalar;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.Token;

public class StructuredValueSerializerTest
	extends ValueSerializerTest
{
	@Test
	public void testReadField()
		throws IOException
	{
		var def = ObjectDef.create("Object")
			.addField(FieldDef.create("name")
				.withType(ScalarDef.STRING)
				.build()
			)
			.build();

		var instance = new StructuredValueSerializer(
			def,
			Maps.immutable.of(
				"name", new ScalarValueSerializer(new StringScalar())
			)
		);

		var in = write(out -> {
			out.writeObjectStart();
			out.writeString("name");
			out.writeString("Hello world");
			out.writeObjectEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(
			StructuredValue.create(def)
				.add("name", SimpleValue.createString("Hello world"))
				.build()
		));
	}

	@Test
	public void testSkipUnknownField()
		throws IOException
	{
		var def = ObjectDef.create("Object")
			.addField(FieldDef.create("name")
				.withType(ScalarDef.STRING)
				.build()
			)
			.build();

		var instance = new StructuredValueSerializer(
			def,
			Maps.immutable.of(
				"name", new ScalarValueSerializer(new StringScalar())
			)
		);

		var in = write(out -> {
			out.writeObjectStart();
			out.writeString("name");
			out.writeString("Hello world");
			out.writeString("unknown");
			out.writeInt(100);
			out.writeObjectEnd();
		});

		var value = instance.read(in);
		assertThat(value, is(
			StructuredValue.create(def)
				.add("name", SimpleValue.createString("Hello world"))
				.build()
		));
	}

	@Test
	public void testWriteField()
		throws IOException
	{
		var def = ObjectDef.create("Object")
			.addField(FieldDef.create("name")
				.withType(ScalarDef.STRING)
				.build()
			)
			.build();

		var instance = new StructuredValueSerializer(
			def,
			Maps.immutable.of(
				"name", new ScalarValueSerializer(new StringScalar())
			)
		);

		var in = write(out -> {
			instance.write(
				StructuredValue.create(def)
					.add("name", SimpleValue.createString("Hello world"))
					.build(),
				out
			);
		});

		assertThat(in.next(), is(Token.OBJECT_START));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("name"));

		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("Hello world"));

		assertThat(in.next(), is(Token.OBJECT_END));
	}

	@Test
	public void testWriteSkipsMissingField()
		throws IOException
	{
		var def = ObjectDef.create("Object")
			.addField(FieldDef.create("name")
				.withType(ScalarDef.STRING)
				.build()
			)
			.build();

		var instance = new StructuredValueSerializer(
			def,
			Maps.immutable.of(
				"name", new ScalarValueSerializer(new StringScalar())
			)
		);

		var in = write(out -> {
			instance.write(
				StructuredValue.create(def)
					.build(),
				out
			);
		});

		assertThat(in.next(), is(Token.OBJECT_START));
		assertThat(in.next(), is(Token.OBJECT_END));
	}
}

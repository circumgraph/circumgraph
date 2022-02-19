package com.circumgraph.storage.types.enums;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StorageTest;
import com.circumgraph.storage.mutation.SetEnumValueMutation;

import org.junit.jupiter.api.Test;

public class EnumDefaultTest
	extends StorageTest
{
	@Test
	public void testDefault()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType("TestEnum")
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", "A")
						.build()
					)
					.build()
				)
				.build()
			)
			.addType(EnumDef.create("TestEnum")
				.addValue(EnumValueDef.create("A").build())
				.addValue(EnumValueDef.create("B").build())
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var stored = collection.store(
			collection.newMutation().build()
		).block();

		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("field", SimpleValue.class).get();
		assertThat(createdValue.get(), is("A"));
	}

	@Test
	public void testDefaultCanBeOverridden()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType("TestEnum")
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", "A")
						.build()
					)
					.build()
				)
				.build()
			)
			.addType(EnumDef.create("TestEnum")
				.addValue(EnumValueDef.create("A").build())
				.addValue(EnumValueDef.create("B").build())
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var stored = collection.store(
			collection.newMutation()
				.updateField("field", SetEnumValueMutation.create("B"))
				.build()
		).block();

		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("field", SimpleValue.class).get();
		assertThat(createdValue.get(), is("B"));
	}
}

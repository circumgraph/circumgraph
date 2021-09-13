package com.circumgraph.storage.types.ints;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StorageTest;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.junit.jupiter.api.Test;

public class IntDefaultTest
	extends StorageTest
{
	@Test
	public void testDefault()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType(ScalarDef.INT)
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", 999)
						.build()
					)
					.build()
				)
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
		assertThat(createdValue.get(), is(999));
	}

	@Test
	public void testDefaultString()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType(ScalarDef.INT)
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", "42")
						.build()
					)
					.build()
				)
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
		assertThat(createdValue.get(), is(42));
	}

	@Test
	public void testDefaultCanBeOverridden()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType(ScalarDef.INT)
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", 12)
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var stored = collection.store(
			collection.newMutation()
				.updateField("field", ScalarValueMutation.createInt(20))
				.build()
		).block();

		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("field", SimpleValue.class).get();
		assertThat(createdValue.get(), is(20));
	}
}

package com.circumgraph.storage.types.lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StorageTest;
import com.circumgraph.storage.mutation.ListSetMutation;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;

public class ListDefaultTest
	extends StorageTest
{
	@Test
	public void testDefaultEmpty()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType(ListDef.output(ScalarDef.STRING))
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", Lists.immutable.of())
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

		var createdValue = fetched.getField("field", ListValue.class).get();
		assertThat(createdValue.items(), is(Lists.immutable.of()));
	}

	@Test
	public void testDefaultWithItems()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType(ListDef.output(ScalarDef.STRING))
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", Lists.immutable.of("A", "B"))
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

		var createdValue = fetched.getField("field", ListValue.class).get();
		assertThat(createdValue.items(), is(Lists.immutable.of(
			SimpleValue.createString("A"),
			SimpleValue.createString("B")
		)));
	}

	@Test
	public void testDefaultCanBeOverridden()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType(ListDef.output(ScalarDef.STRING))
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", Lists.immutable.of("A", "B"))
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
				.updateField("field", ListSetMutation.create())
				.build()
		).block();

		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("field", ListValue.class).get();
		assertThat(createdValue.items(), is(Lists.immutable.of()));
	}
}

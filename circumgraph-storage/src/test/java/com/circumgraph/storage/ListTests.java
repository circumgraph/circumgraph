package com.circumgraph.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.mutation.SimpleValueMutation;
import com.circumgraph.values.ListValue;
import com.circumgraph.values.SimpleValue;

import org.junit.jupiter.api.Test;

public class ListTests
	extends StorageTest
{
	@Test
	@SuppressWarnings("unchecked")
	public void testListSetString()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("titles")
					.withType(ListDef.output(ScalarDef.STRING))
					.build()
				)
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("titles", ListSetMutation.create(
				SimpleValueMutation.create("a"),
				SimpleValueMutation.create("b")
			))
			.build();

		var stored = collection.store(mutation).block();

		var titles1 = (ListValue<? extends SimpleValue>) stored.getField("titles", ListValue.class).get();
		assertThat(titles1.items(), contains(
			SimpleValue.createString("a"),
			SimpleValue.createString("b")
		));

		var id = stored.getId();

		var fetched = collection.get(id).block();

		var titles2 = (ListValue<? extends SimpleValue>) fetched.getField("titles", ListValue.class).get();
		assertThat(titles2.items(), contains(
			SimpleValue.createString("a"),
			SimpleValue.createString("b")
		));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testListSetStringUpdate()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("titles")
					.withType(ListDef.output(ScalarDef.STRING))
					.build()
				)
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("titles", ListSetMutation.create(
				SimpleValueMutation.create("a")
			))
			.build();

		var stored = collection.store(mutation).block();

		var titles1 = (ListValue<? extends SimpleValue>) stored.getField("titles", ListValue.class).get();
		assertThat(titles1.items(), contains(
			SimpleValue.createString("a")
		));

		var id = stored.getId();

		var mutation2 = collection.newMutation()
			.updateField("titles", ListSetMutation.create(
				SimpleValueMutation.create("a"),
				SimpleValueMutation.create("b")
			))
			.build();

		collection.store(id, mutation2).block();

		var fetched = collection.get(id).block();

		var titles2 = (ListValue<? extends SimpleValue>) fetched.getField("titles", ListValue.class).get();
		assertThat(titles2.items(), contains(
			SimpleValue.createString("a"),
			SimpleValue.createString("b")
		));
	}
}

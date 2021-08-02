package com.circumgraph.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.mutation.ScalarValueMutation;
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
				ScalarValueMutation.createString("a"),
				ScalarValueMutation.createString("b")
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
				ScalarValueMutation.createString("a")
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
				ScalarValueMutation.createString("a"),
				ScalarValueMutation.createString("b")
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

	@Test
	@SuppressWarnings("unchecked")
	public void testListSetNull()
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
				NullMutation.create(),
				ScalarValueMutation.createString("b")
			))
			.build();

		var stored = collection.store(mutation).block();

		var titles1 = (ListValue<? extends SimpleValue>) stored.getField("titles", ListValue.class).get();
		assertThat(titles1.items(), contains(
			null,
			SimpleValue.createString("b")
		));

		var id = stored.getId();

		var fetched = collection.get(id).block();

		var titles2 = (ListValue<? extends SimpleValue>) fetched.getField("titles", ListValue.class).get();
		assertThat(titles2.items(), contains(
			null,
			SimpleValue.createString("b")
		));
	}

	@Test
	public void testListSetNonNullFails()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("titles")
					.withType(ListDef.output(NonNullDef.output(ScalarDef.STRING)))
					.build()
				)
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		assertThrows(StorageValidationException.class, () -> {
			collection.store(collection.newMutation()
				.updateField("titles", ListSetMutation.create(
					NullMutation.create(),
					ScalarValueMutation.createString("b")
				))
				.build()
			).block();
		});
	}

	@Test
	public void testListSetDefault()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("titles")
					.withType(ListDef.output(NonNullDef.output(ScalarDef.STRING)))
					.build()
				)
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.build();

		var stored = collection.store(mutation).block();

		var titles1 = stored.getField("titles", ListValue.class);
		assertThat(titles1, is(Optional.empty()));

		var id = stored.getId();

		var fetched = collection.get(id).block();

		var titles2 = fetched.getField("titles", ListValue.class);
		assertThat(titles2, is(Optional.empty()));
	}

	@Test
	public void testListSetDefaultNonNull()
	{
		var storage = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("titles")
					.withType(NonNullDef.output(ListDef.output(NonNullDef.output(ScalarDef.STRING))))
					.build()
				)
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.build();

		assertThrows(StorageValidationException.class, () -> {
			collection.store(mutation).block();
		});
	}
}

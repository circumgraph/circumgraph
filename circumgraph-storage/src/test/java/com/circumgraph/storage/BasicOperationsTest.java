package com.circumgraph.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.junit.jupiter.api.Test;

public class BasicOperationsTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();
	}

	@Test
	public void testStore()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("Hello World!"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World!"));
	}

	@Test
	public void testUpdate()
	{
		var collection = storage.get("Test");

		var m1 = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("Hello"))
			.build();

		var stored = collection.store(m1).block();
		var id = stored.getId();

		var m2 = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("Hello World!"))
			.build();

		collection.store(id, m2).block();

		var fetched = collection.get(id).block();

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World!"));
	}

	@Test
	public void testEmptyUpdate()
	{
		var collection = storage.get("Test");

		var m1 = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("Hello"))
			.build();

		var stored = collection.store(m1).block();
		var id = stored.getId();

		var m2 = collection.newMutation()
			.build();

		collection.store(id, m2).block();

		var fetched = collection.get(id).block();

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello"));
	}


	@Test
	public void testUpdateIdFails()
	{
		var collection = storage.get("Test");

		var m1 = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("Hello"))
			.build();

		var stored = collection.store(m1).block();
		var id = stored.getId();

		assertThrows(StorageValidationException.class, () -> {
			var m2 = collection.newMutation()
				.updateField("id", ScalarValueMutation.create(ScalarDef.ID, 100l))
				.build();

			collection.store(id, m2).block();
		});
	}

	@Test
	public void testDelete()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("Hello World!"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		collection.delete(id).block();

		var fetched = collection.get(id).block();
		assertThat(fetched, nullValue());
	}
}

package com.circumgraph.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.junit.jupiter.api.Test;

public class NullableTest
	extends SingleModelTest
{
	@Override
	protected Model createModel()
	{
		return Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("nullableScalar")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();
	}

	@Test
	public void testSaveNoValue()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var nullable = fetched.getField("nullableScalar", SimpleValue.class);
		assertThat(nullable, is(Optional.empty()));
	}

	@Test
	public void testSaveThenClearValue()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("nullableScalar", ScalarValueMutation.createString("value"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var nullable = fetched.getField("nullableScalar", SimpleValue.class).get();
		assertThat(nullable.asString(), is("value"));

		// Clear the value
		var updated = collection.store(id, collection.newMutation()
			.updateField("nullableScalar", NullMutation.create())
			.build()
		).block();

		var fetchedUpdate = collection.get(id).block();
		assertThat(fetchedUpdate, is(updated));

		var updatedNullable = fetchedUpdate.getField("nullableScalar", SimpleValue.class);
		assertThat(updatedNullable, is(Optional.empty()));
	}
}

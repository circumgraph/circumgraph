package com.circumgraph.storage.types.localdates;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.junit.jupiter.api.Test;

public class LocalDateTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("created")
					.withType(ScalarDef.LOCAL_DATE)
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
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_DATE, LocalDate.of(2020, 1, 1)))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("created", SimpleValue.class).get();
		assertThat(createdValue.get(), is(LocalDate.of(2020, 1, 1)));
	}

	@Test
	public void testUpdateNone()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_DATE, LocalDate.of(2020, 1, 1)))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		mutation = collection.newMutation()
			.build();

		stored = collection.store(id, mutation).block();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("created", SimpleValue.class).get();
		assertThat(createdValue.get(), is(LocalDate.of(2020, 1, 1)));
	}

	@Test
	public void testUpdateNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_DATE, LocalDate.of(2020, 1, 1)))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		mutation = collection.newMutation()
			.updateField("created", NullMutation.create())
			.build();

		stored = collection.store(id, mutation).block();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("created", SimpleValue.class);
		assertThat(createdValue, is(Optional.empty()));
	}

	@Test
	public void testUpdateValue()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_DATE, LocalDate.of(2020, 1, 1)))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_DATE, LocalDate.of(2021, 1, 1)))
			.build();

		stored = collection.store(id, mutation).block();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("created", SimpleValue.class).get();
		assertThat(createdValue.get(), is(LocalDate.of(2021, 1, 1)));
	}
}

package com.circumgraph.storage.types.localtimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalTime;
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

public class LocalTimeTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("created")
					.withType(ScalarDef.LOCAL_TIME)
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
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("created", SimpleValue.class).get();
		assertThat(createdValue.get(), is(LocalTime.of(3, 4, 59, 999000000)));
	}

	@Test
	public void testUpdateNone()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		mutation = collection.newMutation()
			.build();

		stored = collection.store(id, mutation).block();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("created", SimpleValue.class).get();
		assertThat(createdValue.get(), is(LocalTime.of(3, 4, 59, 999000000)));
	}

	@Test
	public void testUpdateNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
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
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 5, 59, 998000000)))
			.build();

		stored = collection.store(id, mutation).block();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("created", SimpleValue.class).get();
		assertThat(createdValue.get(), is(LocalTime.of(3, 5, 59, 998000000)));
	}
}

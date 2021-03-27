package com.circumgraph.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.mutation.SimpleValueMutation;
import com.circumgraph.values.SimpleValue;

import org.junit.jupiter.api.Test;

public class BasicOperationsTest
	extends SingleModelTest
{
	@Override
	protected Model createModel()
	{
		return Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements("Entity")
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
		var entity = storage.get("Test");

		var mutation = entity.newMutation()
			.updateField("title", SimpleValueMutation.create("Hello World!"))
			.build();

		var stored = entity.store(mutation).block();
		var id = stored.getId();

		var fetched = entity.get(id).block();
		assertThat(fetched, is(stored));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World!"));
	}

	@Test
	public void testUpdate()
	{
		var entity = storage.get("Test");

		var m1 = entity.newMutation()
			.updateField("title", SimpleValueMutation.create("Hello"))
			.build();

		var stored = entity.store(m1).block();
		var id = stored.getId();

		var m2 = entity.newMutation()
			.updateField("title", SimpleValueMutation.create("Hello World!"))
			.build();

		entity.store(id, m2).block();

		var fetched = entity.get(id).block();

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World!"));
	}

	@Test
	public void testUpdateIdFails()
	{
		var entity = storage.get("Test");

		var m1 = entity.newMutation()
			.updateField("title", SimpleValueMutation.create("Hello"))
			.build();

		var stored = entity.store(m1).block();
		var id = stored.getId();

		assertThrows(StorageException.class, () -> {
			var m2 = entity.newMutation()
				.updateField("id", SimpleValueMutation.create(100l))
				.build();

			entity.store(id, m2).block();
		});
	}

	@Test
	public void testDelete()
	{
		var entity = storage.get("Test");

		var mutation = entity.newMutation()
			.updateField("title", SimpleValueMutation.create("Hello World!"))
			.build();

		var stored = entity.store(mutation).block();
		var id = stored.getId();

		entity.delete(id).block();

		var fetched = entity.get(id).block();
		assertThat(fetched, nullValue());
	}
}

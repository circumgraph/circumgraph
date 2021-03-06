package com.circumgraph.storage;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.values.SimpleValue;

import org.junit.jupiter.api.Test;

public class BlankObjectTest
	extends SingleModelTest
{
	@Override
	protected Model createModel()
	{
		return Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements("Entity")
				.build()
			)
			.build();
	}

	@Test
	public void testStore()
	{
		var entity = storage.get("Test");

		var mutation = entity.newMutation()
			.build();

		var stored = entity.store(mutation).block();

		var idValue = (SimpleValue) stored.getFields().get("id");
		long id = (long) idValue.get();

		var fetched = entity.get(id).block();
		assertThat(fetched, is(stored));
	}
}

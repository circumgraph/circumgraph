package com.circumgraph.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.mutation.SetEnumValueMutation;

import org.junit.jupiter.api.Test;

public class EnumTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("type")
					.withType("Type")
					.build()
				)
				.build()
			)
			.addType(EnumDef.create("Type")
				.addValue(EnumValueDef.create("E1").build())
				.addValue(EnumValueDef.create("E2").build())
				.build()
			)
			.build();
	}

	@Test
	public void testStore()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("type", SetEnumValueMutation.create("E1"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var titleValue = fetched.getField("type", SimpleValue.class).get();
		assertThat(titleValue.get(), is("E1"));
	}
}

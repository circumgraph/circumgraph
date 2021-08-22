package com.circumgraph.storage.types.enums;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.validation.ValidationMessageLevel;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StorageValidationException;
import com.circumgraph.storage.mutation.NullMutation;
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

	@Test
	public void testStoreInvalid()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("type", SetEnumValueMutation.create("E4"))
			.build();

		var e = assertThrows(StorageValidationException.class, () -> {
			collection.store(mutation).block();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("storage:validation:invalid-enum-value"));
		assertThat(msg.getLocation().describe(), is("type"));
		assertThat(msg.getArguments().get("value"), is("E4"));
		assertThat(msg.getArguments().get("enum"), is("Type"));
	}

	@Test
	public void testUpdateNone()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("type", SetEnumValueMutation.create("E1"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		mutation = collection.newMutation()
			.build();

		stored = collection.store(id, mutation).block();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var titleValue = fetched.getField("type", SimpleValue.class).get();
		assertThat(titleValue.get(), is("E1"));
	}

	@Test
	public void testUpdateNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("type", SetEnumValueMutation.create("E1"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		mutation = collection.newMutation()
			.updateField("type", NullMutation.create())
			.build();

		stored = collection.store(id, mutation).block();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var titleValue = fetched.getField("type", SimpleValue.class);
		assertThat(titleValue, is(Optional.empty()));
	}

	@Test
	public void testUpdateValue()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("type", SetEnumValueMutation.create("E1"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		mutation = collection.newMutation()
			.updateField("type", SetEnumValueMutation.create("E2"))
			.build();

		stored = collection.store(id, mutation).block();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var titleValue = fetched.getField("type", SimpleValue.class).get();
		assertThat(titleValue.get(), is("E2"));
	}
}

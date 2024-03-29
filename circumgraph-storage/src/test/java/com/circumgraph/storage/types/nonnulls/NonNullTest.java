package com.circumgraph.storage.types.nonnulls;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.validation.ValidationMessageLevel;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageException;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StorageValidationException;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.junit.jupiter.api.Test;

/**
 * Tests for non-null validation during storing.
 */
public class NonNullTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("nullable")
					.withType(ScalarDef.STRING)
					.build()
				)
				.addField(FieldDef.create("nonNull")
					.withType(NonNullDef.output(ScalarDef.STRING))
					.build()
				)
				.build()
			)
			.build();
	}

	@Test
	public void testSaveNonNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("nonNull", ScalarValueMutation.createString("Value"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var nonNull = fetched.getField("nonNull", SimpleValue.class).get();
		assertThat(nonNull.asString(), is("Value"));

		var nullable = fetched.getField("nullable", SimpleValue.class);
		assertThat(nullable, is(Optional.empty()));
	}

	@Test
	public void testSaveNullableAndNonNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("nonNull", ScalarValueMutation.createString("V1"))
			.updateField("nullable", ScalarValueMutation.createString("V2"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var nonNull = fetched.getField("nonNull", SimpleValue.class).get();
		assertThat(nonNull.asString(), is("V1"));

		var nullable = fetched.getField("nullable", SimpleValue.class).get();
		assertThat(nullable.asString(), is("V2"));
	}

	@Test
	public void testCantSaveMissingValue()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("nullable", ScalarValueMutation.createString("V2"))
			.build();

		assertThrows(StorageException.class, () -> {
			collection.store(mutation).block();
		});
	}

	@Test
	public void testSaveNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("nonNull", NullMutation.create())
			.build();

		var e = assertThrows(StorageValidationException.class, () -> {
			collection.store(mutation).block();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("storage:validation:null"));
		assertThat(msg.getLocation().describe(), is("nonNull"));
	}
}

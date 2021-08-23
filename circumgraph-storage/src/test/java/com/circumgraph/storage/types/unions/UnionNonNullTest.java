package com.circumgraph.storage.types.unions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.model.validation.ValidationMessageLevel;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StorageValidationException;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.mutation.StructuredMutation;

import org.junit.jupiter.api.Test;

public class UnionNonNullTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("value")
					.withType(NonNullDef.output("U"))
					.build()
				)
				.build()
			)
			.addType(UnionDef.create("U")
				.addType(TypeRef.create("U1"))
				.addType(TypeRef.create("U2"))
				.build()
			)
			.addType(ObjectDef.create("U1")
				.addField(FieldDef.create("v1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("U2")
				.addField(FieldDef.create("v2")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();
	}

	@Test
	public void testStoreNone()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.build();

		var e = assertThrows(StorageValidationException.class, () -> {
			collection.store(mutation).block();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("storage:validation:null"));
		assertThat(msg.getLocation().describe(), is("value"));
	}

	@Test
	public void testStoreNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.build();

		var e = assertThrows(StorageValidationException.class, () -> {
			collection.store(mutation).block();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("storage:validation:null"));
		assertThat(msg.getLocation().describe(), is("value"));
	}

	@Test
	public void testStoreU1()
	{
		var collection = storage.get("Test");
		var union = (StructuredDef) model.get("U1").get();

		var mutation = collection.newMutation()
			.updateField("value", StructuredMutation.create(union)
				.updateField("v1", ScalarValueMutation.createString("Hello World"))
				.build())
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var value = fetched.getField("value", StructuredValue.class).get();

		// Check that the type is correct
		assertThat(value.getDefinition(), is(union));

		// Check the field value
		var v1 = value.getField("v1", SimpleValue.class).get();
		assertThat(v1.asString(), is("Hello World"));
	}

	@Test
	public void testStoreU2()
	{
		var collection = storage.get("Test");
		var union = (StructuredDef) model.get("U2").get();

		var mutation = collection.newMutation()
			.updateField("value", StructuredMutation.create(union)
				.updateField("v2", ScalarValueMutation.createString("Hello World"))
				.build())
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var value = fetched.getField("value", StructuredValue.class).get();

		// Check that the type is correct
		assertThat(value.getDefinition(), is(union));

		// Check the field value
		var v1 = value.getField("v2", SimpleValue.class).get();
		assertThat(v1.asString(), is("Hello World"));
	}
}

package com.circumgraph.storage.unions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.SingleModelTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.values.SimpleValue;
import com.circumgraph.values.StructuredValue;

import org.junit.jupiter.api.Test;

/**
 * Tests for a union that references two different objects, no interfaces.
 */
public class UnionTest
	extends SingleModelTest
{
	@Override
	protected Model createModel()
	{
		return Model.create()
			.addSchema(StorageSchema.INSTANCE)
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

	@Test
	public void testUpdateU1()
	{
		var collection = storage.get("Test");
		var union = (StructuredDef) storage.getModel().get("U1").get();

		var mutation = collection.newMutation()
			.updateField("value", StructuredMutation.create(union)
				.updateField("v1", ScalarValueMutation.createString("Hello World"))
				.build())
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		// Perform the update
		var updateMutation = collection.newMutation()
			.updateField("value", StructuredMutation.create(union)
				.updateField("v1", ScalarValueMutation.createString("Hello World!"))
				.build())
			.build();

		var updated = collection.store(id, updateMutation).block();
		var fetchedUpdate = collection.get(id).block();

		assertThat(fetchedUpdate, is(updated));

		var value = fetchedUpdate.getField("value", StructuredValue.class).get();

		// Check that the type is correct
		assertThat(value.getDefinition(), is(union));

		// Check the field value
		var v1 = value.getField("v1", SimpleValue.class).get();
		assertThat(v1.asString(), is("Hello World!"));
	}

	@Test
	public void testUpdateU1toU2()
	{
		var collection = storage.get("Test");
		var u1 = (StructuredDef) storage.getModel().get("U1").get();
		var u2 = (StructuredDef) storage.getModel().get("U2").get();

		var mutation = collection.newMutation()
			.updateField("value", StructuredMutation.create(u1)
				.updateField("v1", ScalarValueMutation.createString("Hello World"))
				.build())
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		// Perform the update
		var updateMutation = collection.newMutation()
			.updateField("value", StructuredMutation.create(u2)
				.updateField("v2", ScalarValueMutation.createString("Hello World!"))
				.build())
			.build();

		var updated = collection.store(id, updateMutation).block();
		var fetchedUpdate = collection.get(id).block();

		assertThat(fetchedUpdate, is(updated));

		var value = fetchedUpdate.getField("value", StructuredValue.class).get();

		// Check that the type of value has changed
		assertThat(value.getDefinition(), is(u2));

		// Ensure that the old field is not available somehow
		assertThat(value.getField("v1"), is(Optional.empty()));

		// Check that the new value is set
		var v2 = value.getField("v2", SimpleValue.class).get();
		assertThat(v2.asString(), is("Hello World!"));
	}
}

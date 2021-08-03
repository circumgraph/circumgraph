package com.circumgraph.storage.unions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;
import com.circumgraph.storage.mutation.StructuredMutation;

import org.junit.jupiter.api.Test;

/**
 * Tests for when unions contain entities in which case they should be
 * referenced instead of stored.
 */
public class UnionRefTest
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
				.addType(TypeRef.create("NonRef"))
				.addType(TypeRef.create("Ref"))
				.build()
			)
			.addType(ObjectDef.create("NonRef")
				.addField(FieldDef.create("v1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Ref")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("v2")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();
	}

	@Test
	public void testStoreNonRef()
	{
		var collection = storage.get("Test");
		var union = (StructuredDef) model.get("NonRef").get();

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
	public void testStoreRef()
	{
		var collection = storage.get("Test");
		var refCollection = storage.get("Ref");

		var e = refCollection.store(refCollection.newMutation()
			.updateField("v2", ScalarValueMutation.createString("Value"))
			.build()
		).block();

		var mutation = collection.newMutation()
			.updateField("value", StoredObjectRefMutation.create(e.getDefinition(), e.getId()))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var value = fetched.getField("value", StoredObjectRef.class).get();
		assertThat(value.getDefinition(), is(refCollection.getDefinition()));
		assertThat(value.getId(), is(e.getId()));
	}
}

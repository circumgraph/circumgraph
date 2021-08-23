package com.circumgraph.storage.types.interfaces;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;

import org.junit.jupiter.api.Test;

public class PolymorphyReferenceTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("ref")
					.withType("R")
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("R")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("T1")
				.addImplements("R")
				.build()
			)
			.addType(InterfaceDef.create("I1")
				.addImplements("R")
				.build()
			)
			.addType(ObjectDef.create("T2")
				.addImplements("I1")
				.build()
			)
			.build();
	}

	@Test
	public void testReferenceT1()
	{
		var refCollection = storage.get("R");

		var t1Def = model.get("T1", StructuredDef.class).get();
		var storedRef = refCollection
			.store(refCollection.newMutation(t1Def)
				.updateField("title", ScalarValueMutation.createString("Hello World"))
				.build()
			)
			.block();

		var refId = storedRef.getId();

		var testCollection = storage.get("Test");
		var stored = testCollection
			.store(testCollection.newMutation()
				.updateField("ref", StoredObjectRefMutation.create(refCollection.getDefinition(), refId))
				.build()
			)
			.block();

		var fetched = testCollection.get(stored.getId()).block();
		assertThat(stored, is(fetched));

		var fetchedRef = fetched.getField("ref", StoredObjectRef.class).get();
		assertThat(fetchedRef.getDefinition(), is(refCollection.getDefinition()));
		assertThat(fetchedRef.getId(), is(refId));
	}

	@Test
	public void testReferenceT2()
	{
		var refCollection = storage.get("R");

		var t1Def = model.get("T2", StructuredDef.class).get();
		var storedRef = refCollection
			.store(refCollection.newMutation(t1Def)
				.updateField("title", ScalarValueMutation.createString("Hello World"))
				.build()
			)
			.block();

		var refId = storedRef.getId();

		var testCollection = storage.get("Test");
		var stored = testCollection
			.store(testCollection.newMutation()
				.updateField("ref", StoredObjectRefMutation.create(refCollection.getDefinition(), refId))
				.build()
			)
			.block();

		var fetched = testCollection.get(stored.getId()).block();
		assertThat(stored, is(fetched));

		var fetchedRef = fetched.getField("ref", StoredObjectRef.class).get();
		assertThat(fetchedRef.getDefinition(), is(refCollection.getDefinition()));
		assertThat(fetchedRef.getId(), is(refId));
	}
}

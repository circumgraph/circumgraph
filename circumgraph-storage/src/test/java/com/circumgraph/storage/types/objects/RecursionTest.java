package com.circumgraph.storage.types.objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StorageTest;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.mutation.StructuredMutation;

import org.junit.jupiter.api.Test;

/**
 * Test to ensure that recursion of types works correctly.
 */
public class RecursionTest
	extends StorageTest
{
	@Test
	public void testReferenceDirectNull()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("node")
					.withType(NonNullDef.output("Node"))
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Node")
				.addField(FieldDef.create("value")
					.withType(ScalarDef.STRING)
					.build()
				)
				.addField(FieldDef.create("other")
					.withType("Node")
					.build()
				)
				.build()
			)
			.build());

		var nodeDef = (StructuredDef) storage.getModel().get("Node").get();
		var collection = storage.get("Test");

		var object = collection.store(collection.newMutation()
			.updateField("node", StructuredMutation.create(nodeDef)
				.updateField("value", ScalarValueMutation.createString("v1"))
				.build()
			)
			.build()
		).block();

		var fetched = collection.get(object.getId()).block();
		assertThat(fetched, is(object));

		var node = fetched.getField("node", StructuredValue.class).get();
		var value = node.getField("value", SimpleValue.class).get().asString();
		assertThat(value, is("v1"));

		var subNode = node.getField("node");
		assertThat(subNode, is(Optional.empty()));
	}

	@Test
	public void testReferenceDirectValue()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("node")
					.withType(NonNullDef.output("Node"))
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Node")
				.addField(FieldDef.create("value")
					.withType(ScalarDef.STRING)
					.build()
				)
				.addField(FieldDef.create("other")
					.withType("Node")
					.build()
				)
				.build()
			)
			.build());

		var nodeDef = (StructuredDef) storage.getModel().get("Node").get();
		var collection = storage.get("Test");

		var object = collection.store(collection.newMutation()
			.updateField("node", StructuredMutation.create(nodeDef)
				.updateField("other", StructuredMutation.create(nodeDef)
					.updateField("value", ScalarValueMutation.createString("v1"))
					.build()
				)
				.build()
			)
			.build()
		).block();

		var fetched = collection.get(object.getId()).block();
		assertThat(fetched, is(object));

		var node = fetched.getField("node", StructuredValue.class).get();

		var subNode = node.getField("other", StructuredValue.class).get();
		var value = subNode.getField("value", SimpleValue.class).get().asString();
		assertThat(value, is("v1"));
	}

	@Test
	public void testReferenceViaUnion()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("node")
					.withType(NonNullDef.output("Node"))
					.build()
				)
				.build()
			)
			.addType(UnionDef.create("U")
				.addType(TypeRef.create("Node"))
				.build()
			)
			.addType(ObjectDef.create("Node")
				.addField(FieldDef.create("value")
					.withType(ScalarDef.STRING)
					.build()
				)
				.addField(FieldDef.create("other")
					.withType("U")
					.build()
				)
				.build()
			)
			.build());

		var nodeDef = (StructuredDef) storage.getModel().get("Node").get();
		var collection = storage.get("Test");

		var object = collection.store(collection.newMutation()
			.updateField("node", StructuredMutation.create(nodeDef)
				.updateField("other", StructuredMutation.create(nodeDef)
					.updateField("value", ScalarValueMutation.createString("v1"))
					.build()
				)
				.build()
			)
			.build()
		).block();

		var fetched = collection.get(object.getId()).block();
		assertThat(fetched, is(object));

		var node = fetched.getField("node", StructuredValue.class).get();

		var subNode = node.getField("other", StructuredValue.class).get();
		var value = subNode.getField("value", SimpleValue.class).get().asString();
		assertThat(value, is("v1"));
	}
}

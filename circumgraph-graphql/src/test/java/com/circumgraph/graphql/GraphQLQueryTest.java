package com.circumgraph.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.values.SimpleValue;
import com.circumgraph.values.StructuredValue;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

import se.l4.ylem.ids.Base62LongIdCodec;
import se.l4.ylem.ids.LongIdCodec;

public class GraphQLQueryTest
	extends GraphQLTest
{
	@Test
	public void testGetPreviouslyStored()
	{
		var ctx = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Embedded")
				.addField(FieldDef.create("age")
					.withType(ScalarDef.INT)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addImplements("Entity")
				.addField(FieldDef.create("title")
					.withType(NonNullDef.output(ScalarDef.STRING))
					.build()
				)
				.addField(FieldDef.create("object")
					.withType("Embedded")
					.build()
				)
				.build()
			)
			.build()
		);

		LongIdCodec<String> idCodec = new Base62LongIdCodec();

		// Store test data
		Collection entity = storage.get("Test");

		StructuredValue stored = entity.store(entity.newMutation()
			.updateField("title", ScalarValueMutation.createString("Hello World"))
			.build()
		).block();

		// Extract the id of the test data
		SimpleValue idValue = (SimpleValue) stored.getFields().get("id");
		long id = (long) idValue.get();
		var encodedId = idCodec.encode(id);

		// Execute a query
		var result = ctx.execute(
			"query($id: ID!) { test { get(id: $id) { id, title } } }",
			Maps.mutable.of(
				"id", encodedId
			)
		);

		// Make sure there are no errors
		result.assertNoErrors();

		// Verify that title is correct
		assertThat(result.pick("test", "get", "title"), is("Hello World"));

		// Verify that the id is correct
		assertThat(result.pick("test", "get", "id"), is(encodedId));
	}

	@Test
	public void testMutation()
	{
		var ctx = open(Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Embedded")
				.addField(FieldDef.create("age")
					.withType(ScalarDef.INT)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addImplements("Entity")
				.addField(FieldDef.create("title")
					.withType(NonNullDef.output(ScalarDef.STRING))
					.build()
				)
				.addField(FieldDef.create("object")
					.withType("Embedded")
					.build()
				)
				.build()
			)
			.build()
		);

		// Store a new object
		var result = ctx.execute(
			"mutation($m: TestMutationInput!) { storeTest(mutation: $m) { title } }",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"title", "Hello World"
				)
			)
		);

		// Make sure there are no errors
		result.assertNoErrors();

		// Verify that test.get.title is correct
		var value = result.pick("storeTest", "title");
		assertThat(value, is("Hello World"));
	}
}

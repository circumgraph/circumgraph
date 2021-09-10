package com.circumgraph.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.circumgraph.storage.Collection;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class BasicGetAndStoreTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				title: String!
			}
		""";
	}

	@Test
	public void testGetPreviouslyStored()
	{
		// Store test data
		Collection entity = storage.get("Test");

		var stored = entity.store(entity.newMutation()
			.updateField("title", ScalarValueMutation.createString("Hello World"))
			.build()
		).block();

		// Extract the id of the test data
		var id = stored.getId();

		// Execute a query
		var result = execute(
			"query($id: ID!) { test { get(id: $id) { id, title } } }",
			Maps.mutable.of(
				"id", id
			)
		);

		// Make sure there are no errors
		result.assertNoErrors();

		// Verify that title is correct
		assertThat(result.pick("test", "get", "title"), is("Hello World"));

		// Verify that the id is correct
		assertThat(result.pick("test", "get", "id"), is(id));
	}

	@Test
	public void testMutationNew()
	{
		// Store a new object
		var result = execute(
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

	@Test
	public void testMutationNewInvalid()
	{
		// Store a new object
		var result = execute(
			"mutation($m: TestMutationInput!) { storeTest(mutation: $m) { title } }",
			Maps.mutable.of(
				"m", Maps.mutable.of()
			)
		);

		// Check that there is one error
		assertThat(result.errors().size(), is(1));
	}

	@Test
	public void testMutationUpdate()
	{
		var mutation = """
			mutation($m: TestMutationInput!, $id: ID) {
				storeTest(id: $id, mutation: $m) {
					id,
					title
				}
			}
		""";

		// Store a new object
		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"title", "Initial"
				)
			)
		);

		// Make sure there are no errors
		result.assertNoErrors();

		// Verify that test.get.title is correct
		var value = result.pick("storeTest", "title");
		assertThat(value, is("Initial"));

		// Mutate the object
		var id = result.pick("storeTest", "id");
		result = execute(
			mutation,
			Maps.mutable.of(
				"id", id,
				"m", Maps.mutable.of(
					"title", "Updated"
				)
			)
		);

		// Update should not have errored
		result.assertNoErrors();

		assertThat(result.pick("storeTest", "title"), is("Updated"));
		assertThat(result.pick("storeTest", "id"), is(id));
	}

	@Test
	public void testMutationUpdateNoChange()
	{
		var mutation = """
			mutation($m: TestMutationInput!, $id: ID) {
				storeTest(id: $id, mutation: $m) {
					id,
					title
				}
			}
		""";

		// Store a new object
		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"title", "Initial"
				)
			)
		);

		// Make sure there are no errors
		result.assertNoErrors();

		// Verify that test.get.title is correct
		var value = result.pick("storeTest", "title");
		assertThat(value, is("Initial"));

		// Mutate the object
		var id = result.pick("storeTest", "id");
		result = execute(
			mutation,
			Maps.mutable.of(
				"id", id,
				"m", Maps.mutable.of()
			)
		);

		// Update should not have errored
		result.assertNoErrors();

		assertThat(result.pick("storeTest", "title"), is("Initial"));
		assertThat(result.pick("storeTest", "id"), is(id));
	}

	@Test
	public void testMutationDelete()
	{
		// Store test data
		Collection entity = storage.get("Test");

		var stored = entity.store(entity.newMutation()
			.updateField("title", ScalarValueMutation.createString("Hello World"))
			.build()
		).block();

		// Extract the id of the test data
		var id = stored.getId();

		// Execute a query
		var result = execute(
			"mutation($id: ID!) { deleteTest(id: $id) { success } }",
			Maps.mutable.of(
				"id", id
			)
		);

		// Make sure there are no errors
		result.assertNoErrors();

		var stored2 = entity.get(stored.getId()).block();
		assertThat(stored2, nullValue());
	}
}

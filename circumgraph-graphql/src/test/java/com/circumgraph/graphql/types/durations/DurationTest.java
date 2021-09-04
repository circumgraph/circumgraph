package com.circumgraph.graphql.types.durations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class DurationTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				length: Duration
			}
		""";
	}

	@Test
	public void testStore()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
					length
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "length"), is("PT2H30M"));
	}

	@Test
	public void testStoreInvalid()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
					length
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"length", "abc"
				)
			)
		);

		var error = result.errors().getFirst();
		assertThat(error, notNullValue());
		assertThat(error.getMessage(), containsString("Invalid Duration format"));
	}

	@Test
	public void testUpdateNone()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
					id
					length
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "length"), is("PT2H30M"));

		var id = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.mutable.of(
				"id", id,
				"m", Maps.mutable.of()
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "length"), is("PT2H30M"));
	}

	@Test
	public void testUpdateNull()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
					length
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.mutable.of(
				"id", id,
				"m", Maps.mutable.of(
					"length", null
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "length"), nullValue());
	}

	@Test
	public void testUpdateValue()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
					length
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.mutable.of(
				"id", id,
				"m", Maps.mutable.of(
					"length", "PT2H31M"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "length"), is("PT2H31M"));
	}
}

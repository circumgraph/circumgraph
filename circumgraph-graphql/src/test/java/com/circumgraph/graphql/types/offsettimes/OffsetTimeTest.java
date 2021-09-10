package com.circumgraph.graphql.types.offsettimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class OffsetTimeTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				created: OffsetTime
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
					created
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"created", "06:07:30.999+01:00"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "created"), is("06:07:30.999+01:00"));
	}

	@Test
	public void testStoreInvalid()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
					created
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"created", "invalid"
				)
			)
		);

		var error = result.errors().getFirst();
		assertThat(error, notNullValue());
		assertThat(error.getMessage(), containsString("Invalid OffsetTime"));
	}

	@Test
	public void testUpdateNone()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
					id,
					created
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "06:07:30.999+01:00"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.mutable.of(
				"id", id,
				"m", Maps.mutable.of()
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "created"), is("06:07:30.999+01:00"));
	}

	@Test
	public void testUpdateNull()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
					id,
					created
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "06:07:30.999+01:00"
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
					"created", null
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "created"), nullValue());
	}

	@Test
	public void testUpdateValue()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
					id,
					created
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "06:07:30.999+01:00"
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
					"created", "06:07:33.999+01:00"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "created"), is("06:07:33.999+01:00"));
	}
}

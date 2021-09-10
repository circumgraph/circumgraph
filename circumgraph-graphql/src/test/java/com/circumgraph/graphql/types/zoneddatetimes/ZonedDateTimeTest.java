package com.circumgraph.graphql.types.zoneddatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class ZonedDateTimeTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				created: ZonedDateTime
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
					"created", "2021-01-01T06:07:30.999Z"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "created"), is("2021-01-01T06:07:30.999Z"));
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
		assertThat(error.getMessage(), containsString("Invalid ZonedDateTime"));
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
					"created", "2021-01-01T06:07:30.999Z"
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

		assertThat(result.pick("storeTest", "created"), is("2021-01-01T06:07:30.999Z"));
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
					"created", "2021-01-01T06:07:30.999Z"
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
					"created", "2021-01-01T06:07:30.999Z"
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
					"created", "2021-01-01T06:00:30.999[Europe/Stockholm]"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "created"), is("2021-01-01T06:00:30.999+01:00[Europe/Stockholm]"));
	}
}

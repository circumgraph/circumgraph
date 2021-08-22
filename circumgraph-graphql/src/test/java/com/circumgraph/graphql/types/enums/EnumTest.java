package com.circumgraph.graphql.types.enums;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class EnumTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			enum State {
				ACTIVE
				INACTIVE
			}

			type Test implements Entity {
				state: State
			}
		""";
	}

	@Test
	public void testStore()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						state
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"state", "ACTIVE"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "state"), is("ACTIVE"));
	}

	@Test
	public void testUpdateNone()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
					id,
					state
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"state", "ACTIVE"
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

		assertThat(result.pick("storeTest", "state"), is("ACTIVE"));
	}

	@Test
	public void testUpdateNull()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
					id,
					state
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"state", "ACTIVE"
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
					"state", null
				)
			)
		);

		result.assertNoErrors();


		assertThat(result.pick("storeTest", "state"), nullValue());
	}

	@Test
	public void testUpdateValue()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
					id,
					state
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"state", "ACTIVE"
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
					"state", "INACTIVE"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "state"), is("INACTIVE"));
	}
}


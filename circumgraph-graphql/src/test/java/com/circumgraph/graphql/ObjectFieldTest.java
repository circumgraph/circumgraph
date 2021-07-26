package com.circumgraph.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class ObjectFieldTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type SubObject {
				value: Int!
			}

			type Test implements Entity {
				sub: SubObject!
			}
		""";
	}

	@Test
	public void testStore()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id,
					sub {
						value
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
						"value", 20
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "sub", "value"), is(20));
	}

	@Test
	public void testUpdate()
	{
		var mutation = """
			mutation($m: TestMutationInput!, $id: ID) {
				storeTest(id: $id, mutation: $m) {
					id,
					sub {
						value
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
						"value", 20
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "sub", "value"), is(20));

		var id = result.pick("storeTest", "id");
		result = ctx.execute(
			mutation,
			Maps.mutable.of(
				"id", id,
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
						"value", 30
					)
				)
			)
		);

		// Update should not have errored
		result.assertNoErrors();

		assertThat(result.pick("storeTest", "sub", "value"), is(30));
	}

	@Test
	public void testUpdateNoChange()
	{
		var mutation = """
			mutation($m: TestMutationInput!, $id: ID) {
				storeTest(id: $id, mutation: $m) {
					id,
					sub {
						value
					}
				}
			}
		""";

		var result = ctx.execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
						"value", 20
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "sub", "value"), is(20));

		var id = result.pick("storeTest", "id");
		result = execute(
			mutation,
			Maps.mutable.of(
				"id", id,
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
					)
				)
			)
		);

		// Update should not have errored
		result.assertNoErrors();

		assertThat(result.pick("storeTest", "sub", "value"), is(20));
	}
}

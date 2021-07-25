package com.circumgraph.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class ListNonNullTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				values: [String!]!
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
					values
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"values", Maps.immutable.of(
						"set", Lists.immutable.of(
							"a",
							"b"
						)
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "values"), contains("a", "b"));
	}


	@Test
	public void testStoreAndGet()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id,
					values
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"values", Maps.immutable.of(
						"set", Lists.immutable.of(
							"a",
							"b"
						)
					)
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		var query = """
			query($id: ID!) {
				test {
					get(id: $id) {
						values
					}
				}
			}
		""";

		result = execute(
			query,
			Maps.immutable.of(
				"id", id
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("test", "get", "values"), contains("a", "b"));
	}
}

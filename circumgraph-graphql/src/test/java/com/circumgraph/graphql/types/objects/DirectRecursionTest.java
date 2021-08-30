package com.circumgraph.graphql.types.objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class DirectRecursionTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				node: Node!
			}

			type Node {
				title: String!

				other: Node
			}
		""";
	}

	@Test
	public void testStore1()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id,
					node {
						title
						other {
							title
						}
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"node", Maps.mutable.of(
						"title", "Level 1"
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "node", "title"), is("Level 1"));
	}

	@Test
	public void testStore2()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id,
					node {
						title
						other {
							title
						}
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"node", Maps.mutable.of(
						"title", "Level 1",
						"other", Maps.mutable.of(
							"title", "Level 2"
						)
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "node", "title"), is("Level 1"));
		assertThat(result.pick("storeTest", "node", "other", "title"), is("Level 2"));
	}
}

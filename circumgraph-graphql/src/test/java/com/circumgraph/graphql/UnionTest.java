package com.circumgraph.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class UnionTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				value: U!
			}

			union U = A | B

			type A {
				valueA: String!
			}

			type B {
				valueB: Int!
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
					value {
						__typename

						... on A {
							valueA
						}
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"value", Maps.immutable.of(
						"A", Maps.immutable.of(
							"valueA", "valueInUnion"
						)
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "value", "__typename"), is("A"));
		assertThat(result.pick("storeTest", "value", "valueA"), is("valueInUnion"));
	}
}

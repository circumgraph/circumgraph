package com.circumgraph.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class ReferenceListTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Entity1 implements Entity {
				other: [Entity2!]!
			}

			type Entity2 implements Entity {
				value: String!
			}
		""";
	}

	@Test
	public void testStoreReference()
	{
		var m1 = """
			mutation($m: Entity2MutationInput!) {
				storeEntity2(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			m1,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"value", "Entity2 value"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeEntity2", "id");

		var m2 = """
			mutation($m: Entity1MutationInput!) {
				storeEntity1(mutation: $m) {
					id,
					other {
						id,
						value
					}
				}
			}
		""";

		result = execute(
			m2,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"other", Maps.immutable.of(
						"set", Lists.mutable.of(
							Maps.immutable.of(
								"id", id
							)
						)
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeEntity1", "other", "0", "id"), is(id));
		assertThat(result.pick("storeEntity1", "other", "0", "value"), is("Entity2 value"));
	}
}

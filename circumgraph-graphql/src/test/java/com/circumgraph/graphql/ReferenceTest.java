package com.circumgraph.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class ReferenceTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Entity1 implements Entity {
				other: Entity2
			}

			type Entity2 implements Entity {
				value: String!
			}
		""";
	}

	@Test
	public void testStoreNoReference()
	{
		var mutation = """
			mutation($m: Entity1MutationInput!) {
				storeEntity1(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of()
			)
		);

		result.assertNoErrors();
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
						"id", id
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeEntity1", "other", "id"), is(id));
		assertThat(result.pick("storeEntity1", "other", "value"), is("Entity2 value"));
	}

	@Test
	public void testResolveReference()
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

		var e2id = result.pick("storeEntity2", "id");

		var m2 = """
			mutation($m: Entity1MutationInput!) {
				storeEntity1(mutation: $m) {
					id
				}
			}
		""";

		result = execute(
			m2,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"other", Maps.immutable.of(
						"id", e2id
					)
				)
			)
		);

		result.assertNoErrors();

		var e1id = result.pick("storeEntity1", "id");

		var q1 = """
			query($id: ID!) {
				entity1 {
					get(id: $id) {
						other {
							id,
							value
						}
					}
				}
			}
		""";

		result = execute(
			q1,
			Maps.immutable.of(
				"id", e1id
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("entity1", "get", "other", "id"), is(e2id));
		assertThat(result.pick("entity1", "get", "other", "value"), is("Entity2 value"));
	}
}

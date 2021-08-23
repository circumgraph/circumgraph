package com.circumgraph.graphql.types.unions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class UnionRefTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				value: U
			}

			union U = A | B

			type A {
				valueA: String!
			}

			type B implements Entity {
				valueB: Int!
			}
		""";
	}

	@Test
	public void testStoreNone()
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
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "value"), nullValue());
	}

	@Test
	public void testStoreNull()
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
					"value", null
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "value"), nullValue());
	}

	@Test
	public void testStoreA()
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
						"a", Maps.immutable.of(
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

	@Test
	public void testStoreB()
	{
		var result = execute("""
			mutation {
				storeB(mutation: { valueB: 100 }) {
					id
				}
			}
		""");

		result.assertNoErrors();
		var bId = result.pick("storeB", "id");

		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id,
					value {
						__typename

						... on B {
							valueB
						}
					}
				}
			}
		""";

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"value", Maps.immutable.of(
						"b", Maps.immutable.of(
							"id", bId
						)
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "value", "__typename"), is("B"));
		assertThat(result.pick("storeTest", "value", "valueB"), is(100));
	}

	@Test
	public void testUpdateNone()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
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
						"a", Maps.immutable.of(
							"valueA", "valueInUnion"
						)
					)
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.immutable.of(
				"id", id,
				"m", Maps.immutable.of(
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "value", "__typename"), is("A"));
		assertThat(result.pick("storeTest", "value", "valueA"), is("valueInUnion"));
	}

	@Test
	public void testUpdateSameType()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
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
						"a", Maps.immutable.of(
							"valueA", "valueInUnion"
						)
					)
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.immutable.of(
				"id", id,
				"m", Maps.immutable.of(
					"value", Maps.immutable.of(
						"a", Maps.immutable.of(
							"valueA", "updatedValueInUnion"
						)
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "value", "__typename"), is("A"));
		assertThat(result.pick("storeTest", "value", "valueA"), is("updatedValueInUnion"));
	}

	@Test
	public void testUpdateSwitchType()
	{
		var result = execute("""
			mutation {
				storeB(mutation: { valueB: 100 }) {
					id
				}
			}
		""");

		result.assertNoErrors();
		var bId = result.pick("storeB", "id");

		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
					id,
					value {
						__typename

						... on A {
							valueA
						}

						... on B {
							valueB
						}
					}
				}
			}
		""";

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"value", Maps.immutable.of(
						"a", Maps.immutable.of(
							"valueA", "valueInUnion"
						)
					)
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.immutable.of(
				"id", id,
				"m", Maps.immutable.of(
					"value", Maps.immutable.of(
						"b", Maps.immutable.of(
							"id", bId
						)
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "value", "__typename"), is("B"));
		assertThat(result.pick("storeTest", "value", "valueB"), is(100));
	}

	@Test
	public void testUpdateNull()
	{
		var mutation = """
			mutation($id: ID, $m: TestMutationInput!) {
				storeTest(id: $id, mutation: $m) {
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
						"a", Maps.immutable.of(
							"valueA", "valueInUnion"
						)
					)
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.immutable.of(
				"id", id,
				"m", Maps.immutable.of(
					"value", null
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "value"), nullValue());
	}
}

package com.circumgraph.graphql.types.unions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class UnionRefSearchTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				value: U @index
			}

			union U = A | B

			type A {
				valueA: String! @index(type: TOKEN)
			}

			type B implements Entity {
				valueB: Int!
			}
		""";
	}

	@Test
	public void testSearchWithAnyTrueMatch()
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

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { value: { any: true } } }
					]) {
						totalCount,

						nodes {
							id
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(1));
		assertThat(result.pick("test", "search", "nodes", "0", "id"), is(id));
	}

	@Test
	public void testSearchWithAnyTrueNoMatch()
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

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { value: { any: true } } }
					]) {
						totalCount,

						nodes {
							id
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(0));
	}

	@Test
	public void testSearchWithAnyFalseMatch()
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

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { value: { any: false } } }
					]) {
						totalCount,

						nodes {
							id
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(1));
		assertThat(result.pick("test", "search", "nodes", "0", "id"), is(id));
	}

	@Test
	public void testSearchWithAnyFalseNoMatch()
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

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { value: { any: false } } }
					]) {
						totalCount,

						nodes {
							id
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(0));
	}

	@Test
	public void testSearchWithSpecificAnyTrueMatch()
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

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{
							field: {
								value: {
									a: { any: true }
								}
							}
						}
					]) {
						totalCount,

						nodes {
							id
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(1));
		assertThat(result.pick("test", "search", "nodes", "0", "id"), is(id));
	}

	@Test
	public void testSearchWithSpecificAnyFalseMatch()
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

						... on A {
							valueA
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

		result = execute("""
			query {
				test {
					search(criteria: [
						{
							field: {
								value: {
									a: { any: true }
								}
							}
						}
					]) {
						totalCount,

						nodes {
							id
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(0));
	}

	@Test
	public void testSearchWithSpecificFieldMatch()
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

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{
							field: {
								value: {
									a: {
										field: {
											valueA: { equals: \"valueInUnion\" }
										}
									}
								}
							}
						}
					]) {
						totalCount,

						nodes {
							id
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(1));
		assertThat(result.pick("test", "search", "nodes", "0", "id"), is(id));
	}

	@Test
	public void testSearchWithRefAnyTrueMatch()
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

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{
							field: {
								value: {
									b: { any: true }
								}
							}
						}
					]) {
						totalCount,

						nodes {
							id
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(1));
		assertThat(result.pick("test", "search", "nodes", "0", "id"), is(id));
	}

	@Test
	public void testSearchWithRefEqualsMatch()
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

		var id = result.pick("storeTest", "id");

		result = execute("""
			query($id: ID) {
				test {
					search(criteria: [
						{
							field: {
								value: {
									b: { equals: $id }
								}
							}
						}
					]) {
						totalCount,

						nodes {
							id
						}
					}
				}
			}
		""", Maps.immutable.of("id", bId));

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(1));
		assertThat(result.pick("test", "search", "nodes", "0", "id"), is(id));
	}
}

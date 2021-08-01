package com.circumgraph.graphql.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class FloatIndexTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				code: Float! @index
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
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 100
				)
			)
		);

		result.assertNoErrors();
	}

	@Test
	public void testQuerySingleNoMatch()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 100
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { code: { equals: 0 } } }
					]) {
						totalCount,

						pageInfo {
							hasNextPage
							hasPreviousPage
						}

						nodes {
							id
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(0));
		assertThat(result.pick("test", "search", "pageInfo", "hasNextPage"), is(false));
		assertThat(result.pick("test", "search", "pageInfo", "hasPreviousPage"), is(false));
	}

	@Test
	public void testQuerySingleMatch()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 100
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { code: { equals: 100 } } }
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
	public void testQueryMultipleOneMatch()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 100
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 102
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { code: { equals: 102 } } }
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
	}

	@Test
	public void testQueryRangeMinMaxInclusive()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 100.2
				)
			)
		);

		result.assertNoErrors();

		var id1 = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 199.2
				)
			)
		);

		result.assertNoErrors();

		var id2 = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { code: { range: { min: 100, max: 200 } } } }
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

		assertThat(result.pick("test", "search", "totalCount"), is(2));
		assertThat(result.pick("test", "search", "nodes", "0", "id"), is(id1));
		assertThat(result.pick("test", "search", "nodes", "1", "id"), is(id2));
	}

	@Test
	public void testQueryRangeMaxExclusive()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 100
				)
			)
		);

		result.assertNoErrors();

		var id1 = result.pick("storeTest", "id");

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 200
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { code: { range: { min: 100, max: 200, maxInclusive: false } } } }
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
		assertThat(result.pick("test", "search", "nodes", "0", "id"), is(id1));
	}

	@Test
	public void testQueryRangeMinExclusive()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 100
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", 200
				)
			)
		);

		result.assertNoErrors();

		var id2 = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { code: { range: { min: 100, minInclusive: false, max: 200 } } } }
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
		assertThat(result.pick("test", "search", "nodes", "0", "id"), is(id2));
	}
}

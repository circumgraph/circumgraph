package com.circumgraph.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class StringTokenSortTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				code: String! @index(type: TOKEN) @sortable
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
					"code", "A"
				)
			)
		);

		result.assertNoErrors();
	}

	@Test
	public void testSortDefaultOrder()
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
					"code", "B"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", "A"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(sort: [
						{ field: CODE }
					]) {
						totalCount,

						nodes {
							id,
							code
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(2));
		assertThat(result.pick("test", "search", "nodes", "0", "code"), is("A"));
		assertThat(result.pick("test", "search", "nodes", "1", "code"), is("B"));
	}

	@Test
	public void testSortAscendingOrder()
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
					"code", "B"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", "A"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(sort: [
						{ field: CODE, ascending: true }
					]) {
						totalCount,

						nodes {
							id,
							code
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(2));
		assertThat(result.pick("test", "search", "nodes", "0", "code"), is("A"));
		assertThat(result.pick("test", "search", "nodes", "1", "code"), is("B"));
	}

	@Test
	public void testSortDescendingOrder()
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
					"code", "B"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"code", "A"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(sort: [
						{ field: CODE, ascending: false }
					]) {
						totalCount,

						nodes {
							id,
							code
						}
					}
				}
			}
		""");

		result.assertNoErrors();

		assertThat(result.pick("test", "search", "totalCount"), is(2));
		assertThat(result.pick("test", "search", "nodes", "0", "code"), is("B"));
		assertThat(result.pick("test", "search", "nodes", "1", "code"), is("A"));
	}
}

package com.circumgraph.graphql.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class StringFullTextIndexTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				title: String! @index(type: FULL_TEXT)
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
					"title", "Hello"
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
					"title", "Hello"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { title: { match: { query: \"na\" } } } }
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
					"title", "Hello"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { title: { match: { query: \"Hello\" } } } }
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
					"title", "Hello"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"title", "Hello2"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { title: { match: { query: \"Hello\" } } } }
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
	public void testQueryMultipleTwoMatches()
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
					"title", "Hello"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"title", "Hello world"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ or: [
							{ field: { title: { match: { query: \"Hello\" } } } }
						] }
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
	}
}

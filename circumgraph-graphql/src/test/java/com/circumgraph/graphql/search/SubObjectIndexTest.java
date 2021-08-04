package com.circumgraph.graphql.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class SubObjectIndexTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type SubObject {
				value: Int! @index
			}

			type Test implements Entity {
				sub: SubObject! @index
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
					sub {
						value
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
						"value", 20
					)
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "sub", "value"), is(20));
	}

	@Test
	public void testQueryNoMatch()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id,
					sub {
						value
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
						"value", 20
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
								sub: {
									field: {
										value: { equals: 0 }
									}
								}
							}
						}
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
	public void testQueryOneMatch()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id,
					sub {
						value
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
						"value", 20
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
								sub: {
									field: {
										value: { equals: 20 }
									}
								}
							}
						}
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

		assertThat(result.pick("test", "search", "totalCount"), is(1));
		assertThat(result.pick("test", "search", "pageInfo", "hasNextPage"), is(false));
		assertThat(result.pick("test", "search", "pageInfo", "hasPreviousPage"), is(false));
	}

	@Test
	public void testQueryAnyTrueMatches()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id,
					sub {
						value
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
						"value", 20
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
								sub: {
									any: true
								}
							}
						}
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

		assertThat(result.pick("test", "search", "totalCount"), is(1));
		assertThat(result.pick("test", "search", "pageInfo", "hasNextPage"), is(false));
		assertThat(result.pick("test", "search", "pageInfo", "hasPreviousPage"), is(false));
	}

	@Test
	public void testQueryAnyFalseDoesNotMatch()
	{
		var mutation = """
			mutation($m: TestMutationInput!) {
				storeTest(mutation: $m) {
					id,
					sub {
						value
					}
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"sub", Maps.mutable.of(
						"value", 20
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
								sub: {
									any: false
								}
							}
						}
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
}

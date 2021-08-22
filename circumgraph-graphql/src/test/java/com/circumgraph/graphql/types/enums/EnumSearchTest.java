package com.circumgraph.graphql.types.enums;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class EnumSearchTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			enum State {
				ACTIVE
				INACTIVE
			}

			type Test implements Entity {
				state: State @index
			}
		""";
	}

	@Test
	public void testStore()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						state
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"state", "ACTIVE"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "state"), is("ACTIVE"));
	}

	@Test
	public void testSearchWithEqualsMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						state
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"state", "ACTIVE"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { state: { equals: \"ACTIVE\" } } }
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
	public void testSearchWithEqualsNoMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						state
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"state", "INACTIVE"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { state: { equals: \"ACTIVE\" } } }
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
	public void testSearchWithEqualsMatchMultiple()
	{
		int n = 10;
		for(int i=0; i<n; i++)
		{
			var result = execute(
				"""
					mutation($m: TestMutationInput!) {
						storeTest(mutation: $m) {
							id,
							state
						}
					}
				""",
				Maps.mutable.of(
					"m", Maps.mutable.of(
						"state", "ACTIVE"
					)
				)
			);

			result.assertNoErrors();
		}

		var result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { state: { equals: \"ACTIVE\" } } }
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

		assertThat(result.pick("test", "search", "totalCount"), is(10));
	}

	@Test
	public void testSearchWithAnyTrueMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						state
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"state", "ACTIVE"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { state: { any: true } } }
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
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						state
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { state: { any: true } } }
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
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						state
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { state: { any: false } } }
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
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						state
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"state", "ACTIVE"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { state: { any: false } } }
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
}


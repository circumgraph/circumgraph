package com.circumgraph.graphql.types.localtimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class LocalTimeSearchTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				created: LocalTime @index
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
					created
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "created"), is("14:10:30"));
	}

	@Test
	public void testSearchWithEqualsMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { equals: \"14:10:30\" } } }
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
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { equals: \"14:10:31\" } } }
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
							created
						}
					}
				""",
				Maps.mutable.of(
					"m", Maps.mutable.of(
						"created", "14:10:30"
					)
				)
			);

			result.assertNoErrors();
		}

		var result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { equals: \"14:10:30\" } } }
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
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { any: true } } }
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
						created
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
						{ field: { created: { any: true } } }
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
						created
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
						{ field: { created: { any: false } } }
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
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { any: false } } }
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
	public void testSearchWithRangeMinMaxInclusiveMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "15:10:30"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"14:10:30\", max: \"15:10:29\" } } } }
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
	public void testSearchWithRangeMinMaxExclusiveMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:31"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"14:10:30\", max: \"14:10:31\", maxInclusive: false } } } }
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
	public void testSearchWithRangeMinMaxInclusiveMatchMultiple()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "15:10:30"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"14:10:30\", max: \"15:10:30\" } } } }
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

	@Test
	public void testSearchWithRangeMinInclusiveMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "15:20:30"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"15:20:30\" } } } }
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
	public void testSearchWithRangeMinExclusiveNoMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:50:30"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"14:50:30\", minInclusive: false } } } }
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
	public void testSearchWithRangeMaxInclusiveMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:31"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { max: \"14:10:30\" } } } }
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
	public void testSearchWithRangeMaxExclusiveMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:30"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						created
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"created", "14:10:31"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { max: \"14:10:31\", maxInclusive: false } } } }
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
}

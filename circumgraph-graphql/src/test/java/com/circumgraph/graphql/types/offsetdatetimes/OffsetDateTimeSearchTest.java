package com.circumgraph.graphql.types.offsetdatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class OffsetDateTimeSearchTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				created: OffsetDateTime @index
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
					"created", "2021-01-01T14:10:30Z"
				)
			)
		);

		result.assertNoErrors();
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
					"created", "2021-01-01T14:10:30Z"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { equals: \"2021-01-01T14:10:30Z\" } } }
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
					"created", "2021-01-01T14:10:30Z"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { equals: \"2021-01-01T14:10:31Z\" } } }
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
						"created", "2021-01-01T14:10:30Z"
					)
				)
			);

			result.assertNoErrors();
		}

		var result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { equals: \"2021-01-01T14:10:30Z\" } } }
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
					"created", "2021-01-01T14:10:30Z"
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
					"created", "2021-01-01T14:10:30Z"
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
					"created", "2021-01-01T14:10:30Z"
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
					"created", "2021-01-01T15:10:30Z"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"2021-01-01T14:10:30Z\", max: \"2021-01-01T15:10:29Z\" } } } }
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
					"created", "2021-01-01T14:10:30Z"
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
					"created", "2021-01-01T14:10:31Z"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"2021-01-01T14:10:30Z\", max: \"2021-01-01T14:10:31Z\", maxInclusive: false } } } }
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
					"created", "2021-01-01T14:10:30Z"
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
					"created", "2021-01-01T15:10:30Z"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"2021-01-01T14:10:30Z\", max: \"2021-01-01T15:10:30Z\" } } } }
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
					"created", "2021-01-01T14:10:30Z"
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
					"created", "2021-01-01T15:20:30Z"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"2021-01-01T15:20:30Z\" } } } }
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
					"created", "2021-01-01T14:10:30Z"
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
					"created", "2021-01-01T14:50:30Z"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { min: \"2021-01-01T14:50:30Z\", minInclusive: false } } } }
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
					"created", "2021-01-01T14:10:30Z"
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
					"created", "2021-01-01T14:10:31Z"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { max: \"2021-01-01T14:10:30Z\" } } } }
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
					"created", "2021-01-01T14:10:30Z"
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
					"created", "2021-01-01T14:10:31Z"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { created: { range: { max: \"2021-01-01T14:10:31Z\", maxInclusive: false } } } }
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

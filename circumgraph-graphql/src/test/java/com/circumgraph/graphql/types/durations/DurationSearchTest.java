package com.circumgraph.graphql.types.durations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class DurationSearchTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Test implements Entity {
				length: Duration @index
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
					length
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("storeTest", "length"), is("PT2H30M"));
	}

	@Test
	public void testSearchWithEqualsMatch()
	{
		var result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { equals: \"PT2H30M\" } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { equals: \"PT2H31M\" } } }
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
							length
						}
					}
				""",
				Maps.mutable.of(
					"m", Maps.mutable.of(
						"length", "PT2H30M"
					)
				)
			);

			result.assertNoErrors();
		}

		var result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { equals: \"PT2H30M\" } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { any: true } } }
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
						length
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
						{ field: { length: { any: true } } }
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
						length
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
						{ field: { length: { any: false } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { any: false } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT3H"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { range: { min: \"PT2H30M\", max: \"PT2H59M\" } } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H31M"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { range: { min: \"PT2H30M\", max: \"PT2H31M\", maxInclusive: false } } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT3H"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { range: { min: \"PT2H30M\", max: \"PT3H\" } } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT3H"
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeTest", "id");

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { range: { min: \"PT3H\" } } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			"""
				mutation($m: TestMutationInput!) {
					storeTest(mutation: $m) {
						id,
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT3H"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { range: { min: \"PT3H\", minInclusive: false } } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H31M"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { range: { max: \"PT2H30M\" } } } }
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H30M"
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
						length
					}
				}
			""",
			Maps.mutable.of(
				"m", Maps.mutable.of(
					"length", "PT2H31M"
				)
			)
		);

		result.assertNoErrors();

		result = execute("""
			query {
				test {
					search(criteria: [
						{ field: { length: { range: { max: \"PT2H31M\", maxInclusive: false } } } }
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

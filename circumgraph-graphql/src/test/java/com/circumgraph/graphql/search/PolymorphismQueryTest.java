package com.circumgraph.graphql.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class PolymorphismQueryTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			interface CreativeWork implements Entity {
				name: String! @index(type: TYPE_AHEAD) @sortable
			}

			type Movie implements CreativeWork {
				directors: [Person!]! @index

				releaseYear: Int! @index
			}

			type Book implements CreativeWork {
				authors: [Person!]! @index
			}

			type Person implements Entity {
				name: String!
			}
		""";
	}

	@Test
	public void testQueryMovieYearOneMatch()
	{
		var mutation = """
			mutation($m: CreativeWorkMutationInput!) {
				storeCreativeWork(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"movie", Maps.immutable.of(
						"name", "Jurassic Park",
						"releaseYear", 1993,
						"directors", Maps.immutable.of(
							"set", Lists.mutable.empty()
						)
					)
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeCreativeWork", "id");

		result = execute(
			"""
				query {
					creativeWork {
						search(criteria: [
							{
								movie: {
									field: {
										releaseYear: { equals: 1993 }
									}
								}
							}
						]) {
							totalCount

							nodes {
								id
								name

								... on Movie {
									releaseYear
								}
							}
						}
					}
				}
			"""
		);

		result.assertNoErrors();

		assertThat(result.pick("creativeWork", "search", "totalCount"), is(1));
		assertThat(result.pick("creativeWork", "search", "nodes", "0", "id"), is(id));
	}

	@Test
	public void testQueryMovieAnyOneMatch()
	{
		var mutation = """
			mutation($m: CreativeWorkMutationInput!) {
				storeCreativeWork(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"movie", Maps.immutable.of(
						"name", "Jurassic Park",
						"releaseYear", 1993,
						"directors", Maps.immutable.of(
							"set", Lists.mutable.empty()
						)
					)
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeCreativeWork", "id");

		result = execute(
			"""
				query {
					creativeWork {
						search(criteria: [
							{
								movie: {
									any: true
								}
							}
						]) {
							totalCount

							nodes {
								id
								name

								... on Movie {
									releaseYear
								}
							}
						}
					}
				}
			"""
		);

		result.assertNoErrors();

		assertThat(result.pick("creativeWork", "search", "totalCount"), is(1));
		assertThat(result.pick("creativeWork", "search", "nodes", "0", "id"), is(id));
	}

	@Test
	public void testQueryMovieAnyFalseOneMatch()
	{
		var mutation = """
			mutation($m: CreativeWorkMutationInput!) {
				storeCreativeWork(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"movie", Maps.immutable.of(
						"name", "Jurassic Park",
						"releaseYear", 1993,
						"directors", Maps.immutable.of(
							"set", Lists.mutable.empty()
						)
					)
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			"""
				query {
					creativeWork {
						search(criteria: [
							{
								movie: {
									any: false
								}
							}
						]) {
							totalCount

							nodes {
								id
								name

								... on Movie {
									releaseYear
								}
							}
						}
					}
				}
			"""
		);

		result.assertNoErrors();

		assertThat(result.pick("creativeWork", "search", "totalCount"), is(0));
	}

	@Test
	public void testQueryBookAnyNoMatches()
	{
		var mutation = """
			mutation($m: CreativeWorkMutationInput!) {
				storeCreativeWork(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"movie", Maps.immutable.of(
						"name", "Jurassic Park",
						"releaseYear", 1993,
						"directors", Maps.immutable.of(
							"set", Lists.mutable.empty()
						)
					)
				)
			)
		);

		result.assertNoErrors();

		result = execute(
			"""
				query {
					creativeWork {
						search(criteria: [
							{
								book: {
									any: true
								}
							}
						]) {
							totalCount

							nodes {
								id
								name
							}
						}
					}
				}
			"""
		);

		result.assertNoErrors();

		assertThat(result.pick("creativeWork", "search", "totalCount"), is(0));
	}
}

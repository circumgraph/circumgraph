package com.circumgraph.graphql.relation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.graphql.SingleSchemaGraphQLTest;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class RelationPropertySingleTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			type Document implements Entity {
				creator: User! @index
			}

			type User implements Entity {
				documents: [Document!]! @relation(field: creator)
			}
		""";
	}

	@Test
	public void testSingle()
	{
		var m1 = """
			mutation($m: UserMutationInput!) {
				storeUser(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			m1,
			Maps.immutable.of(
				"m", Maps.immutable.of(
				)
			)
		);

		result.assertNoErrors();

		var id = result.pick("storeUser", "id");

		var m2 = """
			mutation($m: DocumentMutationInput!) {
				storeDocument(mutation: $m) {
					id

					creator {
						id
					}
				}
			}
		""";

		result = execute(
			m2,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"creator", Maps.immutable.of(
						"id", id
					)
				)
			)
		);

		result.assertNoErrors();

		var docId = result.pick("storeDocument", "id");

		assertThat(result.pick("storeDocument", "creator", "id"), is(id));

		var q1 = """
			query($id: ID!) {
				user {
					get(id: $id) {
						documents {
							id
						}
					}
				}
			}
		""";

		result = execute(
			q1,
			Maps.immutable.of(
				"id", id
			)
		);

		result.assertNoErrors();

		assertThat(result.pick("user", "get", "documents", "0", "id"), is(docId));
	}
}

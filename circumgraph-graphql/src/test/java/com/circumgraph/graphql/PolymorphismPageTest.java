package com.circumgraph.graphql;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class PolymorphismPageTest
	extends SingleSchemaGraphQLTest
{
	@Override
	protected String getSchema()
	{
		return """
			interface Page implements Entity {
				title: String! @index(type: TYPE_AHEAD)

				parent: Page @index

				# children: [Page] @relation(field: parent)
			}

			type HomePage implements Page {
				intro: String!
			}

			type ProductsPage implements Page {
			}

			type BlogPost implements Page {
				body: String!
			}
		""";
	}

	@Test
	public void testStoreHomePage()
	{
		var mutation = """
			mutation($m: PageMutationInput!) {
				storePage(mutation: $m) {
					id
				}
			}
		""";

		var result = execute(
			mutation,
			Maps.immutable.of(
				"m", Maps.immutable.of(
					"HomePage", Maps.immutable.of(
						"title", "Home Page",
						"intro", "Example intro text"
					)
				)
			)
		);

		result.assertNoErrors();
	}
}

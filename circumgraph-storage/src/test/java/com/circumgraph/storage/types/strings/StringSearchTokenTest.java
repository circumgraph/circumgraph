package com.circumgraph.storage.types.strings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.EqualsMatcher;

public class StringSearchTokenTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Book")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("isbn")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
						.addArgument("type", "TOKEN")
						.build()
					)
					.build()
				)
				.build()
			)
			.build();
	}

	@Test
	public void testStore()
	{
		var collection = storage.get("Book");

		var mutation = collection.newMutation()
			.updateField("isbn", ScalarValueMutation.createString("076790818X"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));
	}

	@Test
	public void testQueryNoClauses()
	{
		var collection = storage.get("Book");

		var mutation = collection.newMutation()
			.updateField("isbn", ScalarValueMutation.createString("076790818X"))
			.build();

		collection.store(mutation).block();

		var results = collection.search(Query.create()).block();
		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryNoMatches()
	{
		var collection = storage.get("Book");

		var mutation = collection.newMutation()
			.updateField("isbn", ScalarValueMutation.createString("076790818X"))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("isbn").toQuery(EqualsMatcher.create("a")))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQueryToken()
	{
		var collection = storage.get("Book");

		var mutation = collection.newMutation()
			.updateField("isbn", ScalarValueMutation.createString("076790818X"))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("isbn").toQuery(EqualsMatcher.create("076790818X")))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryNull()
	{
		var collection = storage.get("Book");

		var mutation = collection.newMutation()
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("isbn").toQuery(EqualsMatcher.create(null)))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}
}

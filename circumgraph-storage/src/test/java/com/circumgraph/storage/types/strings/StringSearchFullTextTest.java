package com.circumgraph.storage.types.strings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.search.query.UserQuery;

public class StringSearchFullTextTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Book")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
						.addArgument("type", "FULL_TEXT")
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
			.updateField("title", ScalarValueMutation.createString("A Short History of Nearly Everything"))
			.build();

		var stored = collection.store(mutation).block();

		var idValue = (SimpleValue) stored.getFields().get("id");
		long id = (long) idValue.get();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("A Short History of Nearly Everything"));
	}

	@Test
	public void testQueryNoClauses()
	{
		var collection = storage.get("Book");

		var mutation = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("A Short History of Nearly Everything"))
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
			.updateField("title", ScalarValueMutation.createString("A Short History of Nearly Everything"))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("title").toQuery(UserQuery.matcher("test")))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQueryOneMatches()
	{
		var collection = storage.get("Book");

		var mutation = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("A Short History of Nearly Everything"))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("title").toQuery(UserQuery.matcher("everything")))
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
				.addClause(root.field("title").toQuery(EqualsMatcher.create(null)))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}
}

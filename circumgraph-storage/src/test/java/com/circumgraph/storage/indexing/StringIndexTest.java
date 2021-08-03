package com.circumgraph.storage.indexing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.search.Query;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.search.query.FieldQuery;

public class StringIndexTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Book")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("title")
					.withType(NonNullDef.output(ScalarDef.STRING))
					.addDirective(DirectiveUse.create("index")
						.addArgument("type", "FULL_TEXT")
						.build()
					)
					.addDirective(DirectiveUse.create("sortable")
						.build()
					)
					.build()
				)
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
			.updateField("title", ScalarValueMutation.createString("A Short History of Nearly Everything"))
			.updateField("isbn", ScalarValueMutation.createString("076790818X"))
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
			.updateField("title", ScalarValueMutation.createString("A Short History of Nearly Everything"))
			.updateField("isbn", ScalarValueMutation.createString("076790818X"))
			.build();

		collection.store(mutation).block();

		var results = collection.search(
			Query.create()
				.addClause(FieldQuery.create("_.isbn", EqualsMatcher.create("a")))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQueryToken()
	{
		var collection = storage.get("Book");

		var mutation = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("A Short History of Nearly Everything"))
			.updateField("isbn", ScalarValueMutation.createString("076790818X"))
			.build();

		collection.store(mutation).block();

		var results = collection.search(
			Query.create()
				.addClause(FieldQuery.create("_.isbn", EqualsMatcher.create("076790818X")))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryNull()
	{
		var collection = storage.get("Book");

		var mutation = collection.newMutation()
			.updateField("title", ScalarValueMutation.createString("A Short History of Nearly Everything"))
			.build();

		collection.store(mutation).block();

		var results = collection.search(
			Query.create()
				.addClause(FieldQuery.create("_.isbn", EqualsMatcher.create(null)))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}
}

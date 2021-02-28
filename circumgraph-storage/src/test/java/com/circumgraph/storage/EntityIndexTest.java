package com.circumgraph.storage;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.mutation.SimpleValueMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.values.SimpleValue;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.search.query.FieldQuery;

public class EntityIndexTest
	extends StorageTest
{
	@Override
	protected Model createModel()
	{
		return Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Book")
				.addImplements("Entity")
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
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
		var entity = storage.get("Book");

		var mutation = entity.newMutation()
			.updateField("title", SimpleValueMutation.create("A Short History of Nearly Everything"))
			.updateField("isbn", SimpleValueMutation.create("076790818X"))
			.build();

		var stored = entity.store(mutation).block();

		var idValue = (SimpleValue) stored.getFields().get("id");
		long id = (long) idValue.get();

		var fetched = entity.get(id).block();
		assertThat(fetched, is(stored));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("A Short History of Nearly Everything"));
	}

	@Test
	public void testQueryNoClauses()
	{
		var entity = storage.get("Book");

		var mutation = entity.newMutation()
			.updateField("title", SimpleValueMutation.create("A Short History of Nearly Everything"))
			.updateField("isbn", SimpleValueMutation.create("076790818X"))
			.build();

		entity.store(mutation).block();

		var results = entity.search(Query.create()).block();
		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryNoMatches()
	{
		var entity = storage.get("Book");

		var mutation = entity.newMutation()
			.updateField("title", SimpleValueMutation.create("A Short History of Nearly Everything"))
			.updateField("isbn", SimpleValueMutation.create("076790818X"))
			.build();

		entity.store(mutation).block();

		var results = entity.search(
			Query.create()
				.addClause(FieldQuery.create("isbn", EqualsMatcher.create("a")))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQueryToken()
	{
		var entity = storage.get("Book");

		var mutation = entity.newMutation()
			.updateField("title", SimpleValueMutation.create("A Short History of Nearly Everything"))
			.updateField("isbn", SimpleValueMutation.create("076790818X"))
			.build();

		entity.store(mutation).block();

		var results = entity.search(
			Query.create()
				.addClause(FieldQuery.create("isbn", EqualsMatcher.create("076790818X")))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}
}

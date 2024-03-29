package com.circumgraph.storage.types.lists.strings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.ListValue;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.EqualsMatcher;

public class ListStringIndexTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("tags")
					.withType(NonNullDef.output(
						ListDef.output(
							NonNullDef.output(ScalarDef.STRING)
						)
					))
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
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("tags", ListSetMutation.create(
				ScalarValueMutation.createString("a"),
				ScalarValueMutation.createString("b")
			))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var tags = (ListValue<? extends SimpleValue>) fetched.getFields().get("tags");
		assertThat(tags.items(), contains(
			SimpleValue.createString("a"),
			SimpleValue.createString("b")
		));
	}

	@Test
	public void testQueryNoClauses()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("tags", ListSetMutation.create(
				ScalarValueMutation.createString("a"),
				ScalarValueMutation.createString("b")
			))
			.build();

		collection.store(mutation).block();

		var results = collection.search(Query.create()).block();
		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryNoMatches()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("tags", ListSetMutation.create(
				ScalarValueMutation.createString("a"),
				ScalarValueMutation.createString("b")
			))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("tags").toQuery(EqualsMatcher.create("na")))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQueryMatch()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("tags", ListSetMutation.create(
				ScalarValueMutation.createString("a"),
				ScalarValueMutation.createString("b")
			))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("tags").toQuery(EqualsMatcher.create("b")))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}
}

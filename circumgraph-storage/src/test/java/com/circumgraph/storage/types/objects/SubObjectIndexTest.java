package com.circumgraph.storage.types.objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StorageSearchException;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.AnyMatcher;
import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.NullMatcher;

public class SubObjectIndexTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("indexed")
					.withType("Sub")
					.addDirective(DirectiveUse.create("index")
						.build()
					)
					.build()
				)
				.addField(FieldDef.create("nonIndexed")
					.withType("Sub")
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Sub")
				.addField(FieldDef.create("value")
					.withType(NonNullDef.output(ScalarDef.STRING))
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
		var subDef = (StructuredDef) storage.getModel().get("Sub").get();

		var mutation = collection.newMutation()
			.updateField("indexed", StructuredMutation.create(subDef)
				.updateField("value", ScalarValueMutation.createString("a1"))
				.build())
			.build();

		var stored = collection.store(mutation).block();

		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));
	}

	@Test
	public void testQueryNoClauses()
	{
		var collection = storage.get("Test");
		var subDef = (StructuredDef) storage.getModel().get("Sub").get();

		var mutation = collection.newMutation()
			.updateField("indexed", StructuredMutation.create(subDef)
				.updateField("value", ScalarValueMutation.createString("a1"))
				.build())
			.build();

		collection.store(mutation).block();

		var results = collection.search(Query.create()).block();
		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryNoMatches()
	{
		var collection = storage.get("Test");
		var subDef = (StructuredDef) storage.getModel().get("Sub").get();

		var mutation = collection.newMutation()
			.updateField("indexed", StructuredMutation.create(subDef)
				.updateField("value", ScalarValueMutation.createString("a1"))
				.build())
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("indexed").field("value").toQuery(EqualsMatcher.create("b1")))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQuerySingleMatch()
	{
		var collection = storage.get("Test");
		var subDef = (StructuredDef) storage.getModel().get("Sub").get();

		var mutation = collection.newMutation()
			.updateField("indexed", StructuredMutation.create(subDef)
				.updateField("value", ScalarValueMutation.createString("a1"))
				.build())
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("indexed").field("value").toQuery(EqualsMatcher.create("a1")))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryNullMatchesNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("indexed").toQuery(NullMatcher.create()))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryNullDoesNotMatchAny()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("indexed").toQuery(AnyMatcher.create()))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQueryNonIndexed()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		assertThrows(StorageSearchException.class, () -> {
			collection.search(
				Query.create()
					.addClause(root.field("nonIndexed").toQuery(NullMatcher.create()))
			).block();
		});
	}
}

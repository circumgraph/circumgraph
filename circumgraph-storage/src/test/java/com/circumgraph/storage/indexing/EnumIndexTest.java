package com.circumgraph.storage.indexing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.SetEnumValueMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.AnyMatcher;
import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.NullMatcher;

public class EnumIndexTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("indexed")
					.withType("TestEnum")
					.addDirective(DirectiveUse.create("index")
						.build()
					)
					.build()
				)
				.addField(FieldDef.create("nonIndexed")
					.withType("TestEnum")
					.build()
				)
				.build()
			)
			.addType(EnumDef.create("TestEnum")
				.addValue(EnumValueDef.create("A").build())
				.addValue(EnumValueDef.create("B").build())
				.build()
			)
			.build();
	}

	@Test
	public void testStore()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("indexed", SetEnumValueMutation.create("A"))
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

		var mutation = collection.newMutation()
			.updateField("indexed", SetEnumValueMutation.create("A"))
			.build();

		collection.store(mutation).block();

		var results = collection.search(Query.create()).block();
		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQuerySingleMatch()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("indexed", SetEnumValueMutation.create("A"))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("indexed").toQuery(EqualsMatcher.create("A")))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryNoMatch()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("indexed", SetEnumValueMutation.create("A"))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("indexed").toQuery(EqualsMatcher.create("B")))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQueryAnyMatchesNonNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("indexed", SetEnumValueMutation.create("A"))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("indexed").toQuery(AnyMatcher.create()))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryAnyDoestNotMatchNull()
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
	public void testQueryNullDoesNotMatchNonNull()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("indexed", SetEnumValueMutation.create("A"))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(root.field("indexed").toQuery(NullMatcher.create()))
		).block();

		assertThat(results.getTotalCount(), is(0));
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
}

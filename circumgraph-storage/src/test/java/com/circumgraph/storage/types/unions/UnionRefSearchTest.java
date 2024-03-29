package com.circumgraph.storage.types.unions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.ArgumentUse;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.AnyMatcher;

public class UnionRefSearchTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("value")
					.withType(NonNullDef.output("U"))
					.addDirective(DirectiveUse.create("index").build())
					.build()
				)
				.build()
			)
			.addType(UnionDef.create("U")
				.addType(TypeRef.create("NonRef"))
				.addType(TypeRef.create("Ref"))
				.build()
			)
			.addType(ObjectDef.create("NonRef")
				.addField(FieldDef.create("v1")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("index")
						.addArgument(ArgumentUse.create("type", "TOKEN"))
						.build())
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Ref")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("v2")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();
	}

	@Test
	public void testSearchNonRefAnyTrueMatch()
	{
		var collection = storage.get("Test");
		var union = (StructuredDef) model.get("NonRef").get();

		var mutation = collection.newMutation()
			.updateField("value", StructuredMutation.create(union)
				.updateField("v1", ScalarValueMutation.createString("Hello World"))
				.build())
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var rootPath = QueryPath.root(collection.getDefinition())
			.field("value");
		var results = collection.search(
			Query.create()
				.addClause(rootPath.toQuery(AnyMatcher.create()))
		).block();

		assertThat(results.getTotalCount(), is(1));
		assertThat(results.getNodes().getFirst().getId(), is(id));
	}

	@Test
	public void testSearchRefAnyTrueMatch()
	{
		var collection = storage.get("Test");
		var refCollection = storage.get("Ref");

		var e = refCollection.store(refCollection.newMutation()
			.updateField("v2", ScalarValueMutation.createString("Value"))
			.build()
		).block();

		var mutation = collection.newMutation()
			.updateField("value", StoredObjectRefMutation.create(e.getDefinition(), e.getId()))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var rootPath = QueryPath.root(collection.getDefinition())
			.field("value");
		var results = collection.search(
			Query.create()
				.addClause(rootPath.toQuery(AnyMatcher.create()))
		).block();

		assertThat(results.getTotalCount(), is(1));
		assertThat(results.getNodes().getFirst().getId(), is(id));
	}

	@Test
	public void testSearchRefSpecificAnyTrueMatch()
	{
		var collection = storage.get("Test");
		var refCollection = storage.get("Ref");

		var e = refCollection.store(refCollection.newMutation()
			.updateField("v2", ScalarValueMutation.createString("Value"))
			.build()
		).block();

		var mutation = collection.newMutation()
			.updateField("value", StoredObjectRefMutation.create(e.getDefinition(), e.getId()))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var rootPath = QueryPath.root(collection.getDefinition())
			.field("value");
		var results = collection.search(
			Query.create()
				.addClause(rootPath.polymorphic(refCollection.getDefinition()).toQuery(AnyMatcher.create()))
		).block();

		assertThat(results.getTotalCount(), is(1));
		assertThat(results.getNodes().getFirst().getId(), is(id));
	}
}

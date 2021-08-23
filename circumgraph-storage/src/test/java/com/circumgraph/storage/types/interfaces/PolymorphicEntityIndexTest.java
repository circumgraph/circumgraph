package com.circumgraph.storage.types.interfaces;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.AnyMatcher;
import se.l4.silo.index.EqualsMatcher;

public class PolymorphicEntityIndexTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(InterfaceDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("shared")
					.withType(NonNullDef.output(ScalarDef.STRING))
					.addDirective(DirectiveUse.create("index")
						.addArgument("type", "TOKEN")
						.build()
					)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("A")
				.addImplements("Test")
				.addField(FieldDef.create("inA")
					.withType(NonNullDef.output(ScalarDef.INT))
					.addDirective(DirectiveUse.create("index")
						.build()
					)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("B")
				.addImplements("Test")
				.addField(FieldDef.create("inB")
					.withType(NonNullDef.output(ScalarDef.BOOLEAN))
					.addDirective(DirectiveUse.create("index")
						.build()
					)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("C")
				.addImplements("Test")
				.addField(FieldDef.create("inC")
					.withType(NonNullDef.output(ScalarDef.INT))
					.addDirective(DirectiveUse.create("index")
						.build()
					)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("C_A")
				.addImplements("C")
				.addField(FieldDef.create("inCA")
					.withType(NonNullDef.output(ScalarDef.INT))
					.addDirective(DirectiveUse.create("index")
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
		var typeA = storage.getModel().get("A", StructuredDef.class).get();
		var collection = storage.get("Test");

		var mutation = collection.newMutation(typeA)
			.updateField("shared", ScalarValueMutation.createString("token"))
			.updateField("inA", ScalarValueMutation.createInt(10))
			.build();

		var stored = collection.store(mutation).block();
		long id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));
	}

	@Test
	public void testQueryCommon()
	{
		var typeA = storage.getModel().get("A", StructuredDef.class).get();
		var collection = storage.get("Test");

		var mutation = collection.newMutation(typeA)
			.updateField("shared", ScalarValueMutation.createString("token"))
			.updateField("inA", ScalarValueMutation.createInt(10))
			.build();

		var stored = collection.store(mutation).block();
		long id = stored.getId();

		var rootPath = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(rootPath.field("shared").toQuery(EqualsMatcher.create("token")))
		).block();

		assertThat(results.getTotalCount(), is(1));
		assertThat(results.getNodes().getFirst().getId(), is(id));
	}

	@Test
	public void testQueryAnyA()
	{
		var typeA = storage.getModel().get("A", StructuredDef.class).get();
		var collection = storage.get("Test");

		var mutation = collection.newMutation(typeA)
			.updateField("shared", ScalarValueMutation.createString("token"))
			.updateField("inA", ScalarValueMutation.createInt(10))
			.build();

		var stored = collection.store(mutation).block();
		long id = stored.getId();

		var rootPath = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(rootPath.polymorphic(typeA).toQuery(AnyMatcher.create()))
		).block();

		assertThat(results.getTotalCount(), is(1));
		assertThat(results.getNodes().getFirst().getId(), is(id));
	}

	@Test
	public void testQueryInA()
	{
		var typeA = storage.getModel().get("A", StructuredDef.class).get();
		var collection = storage.get("Test");

		var mutation = collection.newMutation(typeA)
			.updateField("shared", ScalarValueMutation.createString("token"))
			.updateField("inA", ScalarValueMutation.createInt(10))
			.build();

		var stored = collection.store(mutation).block();
		long id = stored.getId();

		var rootPath = QueryPath.root(collection.getDefinition());
		var results = collection.search(
			Query.create()
				.addClause(rootPath.polymorphic(typeA).field("inA").toQuery(EqualsMatcher.create(10)))
		).block();

		assertThat(results.getTotalCount(), is(1));
		assertThat(results.getNodes().getFirst().getId(), is(id));
	}
}

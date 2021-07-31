package com.circumgraph.storage.indexing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.SingleModelTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ScalarValueMutation;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.search.Query;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.search.query.FieldQuery;

public class SubObjectIndexTest
	extends SingleModelTest
{
	@Override
	protected Model createModel()
	{
		return Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("sub")
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
			.updateField("sub", StructuredMutation.create(subDef)
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
			.updateField("sub", StructuredMutation.create(subDef)
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
			.updateField("sub", StructuredMutation.create(subDef)
				.updateField("value", ScalarValueMutation.createString("a1"))
				.build())
			.build();

		collection.store(mutation).block();

		var results = collection.search(
			Query.create()
				.addClause(FieldQuery.create("_.sub._.value", EqualsMatcher.create("b1")))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQuerySingleMatch()
	{
		var collection = storage.get("Test");
		var subDef = (StructuredDef) storage.getModel().get("Sub").get();

		var mutation = collection.newMutation()
			.updateField("sub", StructuredMutation.create(subDef)
				.updateField("value", ScalarValueMutation.createString("a1"))
				.build())
			.build();

		collection.store(mutation).block();

		var results = collection.search(
			Query.create()
				.addClause(FieldQuery.create("_.sub._.value", EqualsMatcher.create("a1")))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}
}

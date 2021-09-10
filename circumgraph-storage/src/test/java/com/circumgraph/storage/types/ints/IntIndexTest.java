package com.circumgraph.storage.types.ints;

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
import se.l4.silo.index.RangeMatcher;

public class IntIndexTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("value")
					.withType(ScalarDef.INT)
					.addDirective(DirectiveUse.create("index")
						.build()
					)
					.addDirective(DirectiveUse.create("sortable")
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
			.updateField("value", ScalarValueMutation.createInt(10))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var value = fetched.getField("value", SimpleValue.class).get().asInt();
		assertThat(value, is(10));
	}

	@Test
	public void testQueryExact()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("value", ScalarValueMutation.createInt(10))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("value").toQuery(EqualsMatcher.create(10)))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryRangeIncluded()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("value", ScalarValueMutation.createInt(10))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("value").toQuery(RangeMatcher.isMoreThan(9)))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryRangeExcluded()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("value", ScalarValueMutation.createInt(10))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("value").toQuery(RangeMatcher.isMoreThan(11)))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}
}

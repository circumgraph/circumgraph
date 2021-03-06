package com.circumgraph.storage.indexing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.SingleModelTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.SimpleValueMutation;
import com.circumgraph.storage.search.Query;
import com.circumgraph.values.SimpleValue;

import org.junit.jupiter.api.Test;

import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.RangeMatcher;
import se.l4.silo.index.search.query.FieldQuery;

public class FloatIndexTest
	extends SingleModelTest
{
	@Override
	protected Model createModel()
	{
		return Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(ObjectDef.create("Test")
				.addImplements("Entity")
				.addField(FieldDef.create("value")
					.withType(ScalarDef.FLOAT)
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
		var entity = storage.get("Test");

		var mutation = entity.newMutation()
			.updateField("value", SimpleValueMutation.create(10.2))
			.build();

		var stored = entity.store(mutation).block();

		var idValue = (SimpleValue) stored.getFields().get("id");
		long id = (long) idValue.get();

		var fetched = entity.get(id).block();
		assertThat(fetched, is(stored));

		var value = fetched.getField("value", SimpleValue.class).get().asFloat();
		assertThat(value, is(10.2));
	}

	@Test
	public void testQueryExact()
	{
		var entity = storage.get("Test");

		var mutation = entity.newMutation()
			.updateField("value", SimpleValueMutation.create(10.2))
			.build();

		entity.store(mutation).block();

		var results = entity.search(Query.create()
			.addClause(FieldQuery.create("value", EqualsMatcher.create(10.2)))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryRangeIncluded()
	{
		var entity = storage.get("Test");

		var mutation = entity.newMutation()
			.updateField("value", SimpleValueMutation.create(10.2))
			.build();

		entity.store(mutation).block();

		var results = entity.search(Query.create()
			.addClause(FieldQuery.create("value", RangeMatcher.isMoreThan(10.0)))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryRangeExcluded()
	{
		var entity = storage.get("Test");

		var mutation = entity.newMutation()
			.updateField("value", SimpleValueMutation.create(10.2))
			.build();

		entity.store(mutation).block();

		var results = entity.search(Query.create()
			.addClause(FieldQuery.create("value", RangeMatcher.isMoreThan(11.0)))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}
}

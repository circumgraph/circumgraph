package com.circumgraph.storage.types.localtimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalTime;

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

public class LocalTimeSearchTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("created")
					.withType(ScalarDef.LOCAL_TIME)
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
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("created", SimpleValue.class).get();
		assertThat(createdValue.get(), is(LocalTime.of(3, 4, 59, 999000000)));
	}

	@Test
	public void testQueryNoClauses()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
			.build();

		var stored = collection.store(mutation).block();

		var results = collection.search(Query.create()).block();
		assertThat(results.getTotalCount(), is(1));
		assertThat(results.getNodes().getFirst(), is(stored));
	}

	@Test
	public void testQueryExactMatch()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
			.build();

		var stored = collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("created").toQuery(EqualsMatcher.create(LocalTime.of(3, 4, 59, 999000000))))
		).block();

		assertThat(results.getTotalCount(), is(1));
		assertThat(results.getNodes().getFirst(), is(stored));
	}

	@Test
	public void testQueryExactNoMatch()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("created").toQuery(EqualsMatcher.create(LocalTime.of(3, 4, 56, 999000000))))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQueryRangeIncluded()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("created").toQuery(RangeMatcher.isMoreThan(LocalTime.of(3, 4, 59, 998000000))))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryRangeExcluded()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("created", ScalarValueMutation.create(ScalarDef.LOCAL_TIME, LocalTime.of(3, 4, 59, 999000000)))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("created").toQuery(RangeMatcher.isMoreThan(LocalTime.of(3, 5, 59, 999000000))))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}
}

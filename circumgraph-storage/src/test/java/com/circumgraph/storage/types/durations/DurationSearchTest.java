package com.circumgraph.storage.types.durations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.time.Period;

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
import org.threeten.extra.PeriodDuration;

import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.RangeMatcher;

public class DurationSearchTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("length")
					.withType(ScalarDef.DURATION)
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
			.updateField("length", ScalarValueMutation.create(ScalarDef.DURATION, PeriodDuration.of(Period.ofDays(1))))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("length", SimpleValue.class).get();
		assertThat(createdValue.get(), is(PeriodDuration.of(Period.ofDays(1))));
	}

	@Test
	public void testQueryNoClauses()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("length", ScalarValueMutation.create(ScalarDef.DURATION, PeriodDuration.of(Period.ofDays(1))))
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
			.updateField("length", ScalarValueMutation.create(ScalarDef.DURATION, PeriodDuration.of(Period.ofDays(1))))
			.build();

		var stored = collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("length").toQuery(EqualsMatcher.create(PeriodDuration.of(Period.ofDays(1)))))
		).block();

		assertThat(results.getTotalCount(), is(1));
		assertThat(results.getNodes().getFirst(), is(stored));
	}

	@Test
	public void testQueryExactNoMatch()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("length", ScalarValueMutation.create(ScalarDef.DURATION, PeriodDuration.of(Period.ofDays(1))))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("length").toQuery(EqualsMatcher.create(PeriodDuration.of(Period.ofDays(2)))))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}

	@Test
	public void testQueryRangeIncluded()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("length", ScalarValueMutation.create(ScalarDef.DURATION, PeriodDuration.of(Period.ofDays(1))))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("length").toQuery(RangeMatcher.isMoreThan(PeriodDuration.of(Duration.ofSeconds(1)))))
		).block();

		assertThat(results.getTotalCount(), is(1));
	}

	@Test
	public void testQueryRangeExcluded()
	{
		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("length", ScalarValueMutation.create(ScalarDef.DURATION, PeriodDuration.of(Period.ofDays(1))))
			.build();

		collection.store(mutation).block();

		var root = QueryPath.root(collection.getDefinition());
		var results = collection.search(Query.create()
			.addClause(root.field("length").toQuery(RangeMatcher.isMoreThan(PeriodDuration.of(Period.ofDays(1)))))
		).block();

		assertThat(results.getTotalCount(), is(0));
	}
}

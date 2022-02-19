package com.circumgraph.storage.types.zoneddatetimes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StorageTest;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.junit.jupiter.api.Test;

public class OffsetDateTimeDefaultTest
	extends StorageTest
{
	@Test
	public void testDefault()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType(ScalarDef.ZONED_DATE_TIME)
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", "2021-01-01T13:00[Europe/Stockholm]")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var stored = collection.store(
			collection.newMutation().build()
		).block();

		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("field", SimpleValue.class).get();
		assertThat(createdValue.get(), is(ZonedDateTime.of(2021, 1, 1, 13, 0, 0, 0, ZoneId.of("Europe/Stockholm"))));
	}

	@Test
	public void testDefaultCanBeOverridden()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("field")
					.withType(ScalarDef.ZONED_DATE_TIME)
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", "2021-01-01T13:00[Europe/Stockholm]")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var stored = collection.store(
			collection.newMutation()
				.updateField("field", ScalarValueMutation.create(ScalarDef.ZONED_DATE_TIME, ZonedDateTime.of(2021, 1, 1, 15, 0, 0, 0, ZoneId.of("Europe/Stockholm"))))
				.build()
		).block();

		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var createdValue = fetched.getField("field", SimpleValue.class).get();
		assertThat(createdValue.get(), is(ZonedDateTime.of(2021, 1, 1, 15, 0, 0, 0, ZoneId.of("Europe/Stockholm"))));
	}
}

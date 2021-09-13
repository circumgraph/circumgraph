package com.circumgraph.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.junit.jupiter.api.Test;

/**
 * Test that validates that {@code readonly} directives will not allow updates.
 */
public class ReadonlyTest
	extends StorageTest
{
	@Test
	public void testScalarUpdateFails()
	{
		var storage = open(Schema.create()
			.addType(ObjectDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
				.addField(FieldDef.create("value")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("readonly")
						.build()
					)
					.addDirective(DirectiveUse.create("default")
						.addArgument("value", "Initial")
						.build()
					)
					.build()
				)
				.build()
			)
			.build()
		);

		var collection = storage.get("Test");

		var mutation = collection.newMutation()
			.updateField("value", ScalarValueMutation.createString("v1"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		var titleValue = fetched.getField("value", SimpleValue.class).get();
		assertThat(titleValue.get(), is("v1"));

		assertThrows(StorageValidationException.class, () -> {
			var m = collection.newMutation()
				.updateField("value", ScalarValueMutation.createString("v2"))
				.build();

			collection.store(id, m).block();
		});
	}
}

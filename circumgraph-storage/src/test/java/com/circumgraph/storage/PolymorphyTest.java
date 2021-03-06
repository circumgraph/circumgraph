package com.circumgraph.storage;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.mutation.SimpleValueMutation;
import com.circumgraph.values.SimpleValue;

import org.junit.jupiter.api.Test;

public class PolymorphyTest
	extends SingleModelTest
{
	@Override
	protected Model createModel()
	{
		return Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addType(InterfaceDef.create("Test")
				.addImplements("Entity")
				.addField(FieldDef.create("title")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("T1")
				.addImplements("Test")
				.build()
			)
			.addType(ObjectDef.create("T2")
				.addImplements("Test")
				.addField(FieldDef.create("age")
					.withType(ScalarDef.INT)
					.build()
				)
				.build()
			)
			.build();
	}

	@Test
	public void testStoreT1()
	{
		var entity = storage.get("Test");

		var def = (StructuredDef) model.get("T1").get();

		var mutation = entity.newMutation(def)
			.updateField("title", SimpleValueMutation.create("Hello World"))
			.build();

		var stored = entity.store(mutation).block();

		var idValue = (SimpleValue) stored.getFields().get("id");
		long id = (long) idValue.get();

		var fetched = entity.get(id).block();
		assertThat(fetched, is(stored));

		assertThat(fetched.getDefinition(), is(def));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World"));
	}

	@Test
	public void testStoreT2()
	{
		var entity = storage.get("Test");

		var def = (StructuredDef) model.get("T2").get();

		var mutation = entity.newMutation(def)
			.updateField("title", SimpleValueMutation.create("Hello World"))
			.updateField("age", SimpleValueMutation.create(20))
			.build();

		var stored = entity.store(mutation).block();

		var idValue = (SimpleValue) stored.getFields().get("id");
		long id = (long) idValue.get();

		var fetched = entity.get(id).block();
		assertThat(fetched, is(stored));

		assertThat(fetched.getDefinition(), is(def));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World"));

		SimpleValue ageValue = (SimpleValue) fetched.getFields().get("age");
		assertThat(ageValue.get(), is(20));
	}
}

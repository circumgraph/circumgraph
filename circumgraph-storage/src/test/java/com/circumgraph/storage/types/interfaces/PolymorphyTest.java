package com.circumgraph.storage.types.interfaces;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.SingleSchemaTest;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.mutation.ScalarValueMutation;

import org.junit.jupiter.api.Test;

public class PolymorphyTest
	extends SingleSchemaTest
{
	@Override
	protected Schema createSchema()
	{
		return Schema.create()
			.addType(InterfaceDef.create("Test")
				.addImplements(StorageSchema.ENTITY_NAME)
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
			.addType(InterfaceDef.create("I1")
				.addImplements("Test")
				.build()
			)
			.addType(ObjectDef.create("T3")
				.addImplements("I1")
				.build()
			)
			.build();
	}

	@Test
	public void testStoreT1()
	{
		var collection = storage.get("Test");

		var def = (StructuredDef) model.get("T1").get();

		var mutation = collection.newMutation(def)
			.updateField("title", ScalarValueMutation.createString("Hello World"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		assertThat(fetched.getDefinition(), is(def));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World"));
	}

	@Test
	public void testStoreT2()
	{
		var collection = storage.get("Test");

		var def = (StructuredDef) model.get("T2").get();

		var mutation = collection.newMutation(def)
			.updateField("title", ScalarValueMutation.createString("Hello World"))
			.updateField("age", ScalarValueMutation.createInt(20))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		assertThat(fetched.getDefinition(), is(def));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World"));

		SimpleValue ageValue = (SimpleValue) fetched.getFields().get("age");
		assertThat(ageValue.get(), is(20));
	}

	@Test
	public void testStoreT3()
	{
		var collection = storage.get("Test");

		var def = (StructuredDef) model.get("T3").get();

		var mutation = collection.newMutation(def)
			.updateField("title", ScalarValueMutation.createString("Hello World"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		assertThat(fetched.getDefinition(), is(def));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World"));
	}

	@Test
	public void testStoreAndSwitchType()
	{
		var collection = storage.get("Test");

		var def1 = (StructuredDef) model.get("T1").get();

		var mutation = collection.newMutation(def1)
			.updateField("title", ScalarValueMutation.createString("Hello World"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		assertThat(fetched.getDefinition(), is(def1));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World"));

		var def2 = (StructuredDef) model.get("T2").get();

		var mutation2 = collection.newMutation(def2)
			.updateField("title", ScalarValueMutation.createString("Hello World!"))
			.updateField("age", ScalarValueMutation.createInt(20))
			.build();

		var stored2 = collection.store(id, mutation2).block();

		var fetched2 = collection.get(id).block();
		assertThat(fetched2, is(stored2));

		assertThat(fetched2.getDefinition(), is(def2));

		var titleValue2 = (SimpleValue) fetched2.getFields().get("title");
		assertThat(titleValue2.get(), is("Hello World!"));

		SimpleValue ageValue = (SimpleValue) fetched2.getFields().get("age");
		assertThat(ageValue.get(), is(20));
	}

	@Test
	public void testStoreAndSwitchTypeWithInheritedField()
	{
		var collection = storage.get("Test");

		var def1 = (StructuredDef) model.get("T1").get();

		var mutation = collection.newMutation(def1)
			.updateField("title", ScalarValueMutation.createString("Hello World"))
			.build();

		var stored = collection.store(mutation).block();
		var id = stored.getId();

		var fetched = collection.get(id).block();
		assertThat(fetched, is(stored));

		assertThat(fetched.getDefinition(), is(def1));

		var titleValue = (SimpleValue) fetched.getFields().get("title");
		assertThat(titleValue.get(), is("Hello World"));

		var def2 = (StructuredDef) model.get("T2").get();

		var mutation2 = collection.newMutation(def2)
			.updateField("age", ScalarValueMutation.createInt(20))
			.build();

		var stored2 = collection.store(id, mutation2).block();

		var fetched2 = collection.get(id).block();
		assertThat(fetched2, is(stored2));

		assertThat(fetched2.getDefinition(), is(def2));

		var titleValue2 = (SimpleValue) fetched2.getFields().get("title");
		assertThat(titleValue2.get(), is("Hello World"));

		SimpleValue ageValue = (SimpleValue) fetched2.getFields().get("age");
		assertThat(ageValue.get(), is(20));
	}
}

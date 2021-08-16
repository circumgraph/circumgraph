package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.processing.TypeDefProcessor;

import org.junit.jupiter.api.Test;

public class ProcessingTest
{
	@Test
	public void testAddType()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addTypeDefProcessor(new TypeDefProcessor<InterfaceDef>()
			{
				@Override
				public Class<InterfaceDef> getType()
				{
					return InterfaceDef.class;
				}

				@Override
				public void process(
					ProcessingEncounter encounter,
					InterfaceDef type
				)
				{
					if(type.getName().endsWith("Query")) return;

					encounter.addType(InterfaceDef.create(type.getName() + "Query")
						.addField(FieldDef.create("f1")
							.withType(ScalarDef.INT)
							.build()
						)
						.build()
					);
				}
			})
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var query = model.get("TestQuery", InterfaceDef.class).get();

		var field = query.getField("f1").get();
		assertThat(field.getType(), is(ScalarDef.INT));
	}

	@Test
	public void testAddTypeMultipleRuns()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addTypeDefProcessor(new TypeDefProcessor<InterfaceDef>()
			{
				@Override
				public Class<InterfaceDef> getType()
				{
					return InterfaceDef.class;
				}

				@Override
				public void process(
					ProcessingEncounter encounter,
					InterfaceDef type
				)
				{
					if(type.getName().endsWith("QueryQuery")) return;

					encounter.addType(InterfaceDef.create(type.getName() + "Query")
						.addField(FieldDef.create("f1")
							.withType(ScalarDef.INT)
							.build()
						)
						.build()
					);
				}
			})
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var query = model.get("TestQuery", InterfaceDef.class).get();
		var f1 = query.getField("f1").get();
		assertThat(f1.getType(), is(ScalarDef.INT));

		var queryQuery = model.get("TestQueryQuery", InterfaceDef.class).get();
		var f2 = queryQuery.getField("f1").get();
		assertThat(f2.getType(), is(ScalarDef.INT));
	}

	@Test
	public void testReplaceType()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addTypeDefProcessor(new TypeDefProcessor<InterfaceDef>()
			{
				@Override
				public Class<InterfaceDef> getType()
				{
					return InterfaceDef.class;
				}

				@Override
				public void process(
					ProcessingEncounter encounter,
					InterfaceDef type
				)
				{
					encounter.replaceType(InterfaceDef.create("Test")
						.addField(FieldDef.create("f1")
							.withType(ScalarDef.INT)
							.build()
						)
						.build()
					);
				}
			})
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InterfaceDef.class).get();
		var field = type.getField("f1").get();
		assertThat(field.getType(), is(ScalarDef.INT));
	}

	@Test
	public void testChangeFieldOutputType()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addTypeDefProcessor(new TypeDefProcessor<InterfaceDef>()
			{
				@Override
				public Class<InterfaceDef> getType()
				{
					return InterfaceDef.class;
				}

				@Override
				public void process(
					ProcessingEncounter encounter,
					InterfaceDef type
				)
				{
					encounter.changeOutput(type.getField("f1").get(), ScalarDef.INT);
				}
			})
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InterfaceDef.class).get();
		var field = type.getField("f1").get();
		assertThat(field.getType(), is(ScalarDef.INT));
	}

	@Test
	public void testAddArgument()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addTypeDefProcessor(new TypeDefProcessor<InterfaceDef>()
			{
				@Override
				public Class<InterfaceDef> getType()
				{
					return InterfaceDef.class;
				}

				@Override
				public void process(
					ProcessingEncounter encounter,
					InterfaceDef type
				)
				{
					encounter.addArgument(type.getField("f1").get(), ArgumentDef.create("a1")
						.withType(ScalarDef.STRING)
						.build()
					);
				}
			})
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InterfaceDef.class).get();
		var field = type.getField("f1").get();
		assertThat(field.getType(), is(ScalarDef.STRING));

		var arg = field.getArgument("a1").get();
		assertThat(arg.getType(), is(ScalarDef.STRING));
	}
}

package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.processing.TypeDefProcessor;

import org.junit.jupiter.api.Test;

public class ProcessingTest
{
	private static final Location TEST_LOCATION = Location.create("test");

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
				public Location getLocation()
				{
					return TEST_LOCATION;
				}

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
		assertThat(query.getDefinedAt(), is(TEST_LOCATION));

		var field = query.getField("f1").get();
		assertThat(field.getType(), is(ScalarDef.INT));
		assertThat(field.getDefinedAt(), is(TEST_LOCATION));
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
				public Location getLocation()
				{
					return TEST_LOCATION;
				}

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
		assertThat(query.getDefinedAt(), is(TEST_LOCATION));

		var f1 = query.getField("f1").get();
		assertThat(f1.getType(), is(ScalarDef.INT));
		assertThat(f1.getDefinedAt(), is(TEST_LOCATION));

		var queryQuery = model.get("TestQueryQuery", InterfaceDef.class).get();
		assertThat(queryQuery.getDefinedAt(), is(TEST_LOCATION));

		var f2 = queryQuery.getField("f1").get();
		assertThat(f2.getType(), is(ScalarDef.INT));
		assertThat(f1.getDefinedAt(), is(TEST_LOCATION));
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
				public Location getLocation()
				{
					return TEST_LOCATION;
				}

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
		assertThat(type.getDefinedAt(), is(TEST_LOCATION));

		var field = type.getField("f1").get();
		assertThat(field.getType(), is(ScalarDef.INT));
		assertThat(field.getDefinedAt(), is(TEST_LOCATION));
	}

	@Test
	public void testEditType()
	{
		var loc1 = Location.create("LOC1");
		var loc2 = Location.create("LOC2");

		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.withDefinedAt(loc1)
				.addField(FieldDef.create("f1")
					.withDefinedAt(loc2)
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addTypeDefProcessor(new TypeDefProcessor<InterfaceDef>()
			{
				@Override
				public Location getLocation()
				{
					return TEST_LOCATION;
				}

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
					encounter.edit(type, builder -> builder.withDescription("description"));
				}
			})
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InterfaceDef.class).get();
		assertThat(type.getDefinedAt(), is(MergedLocation.of(loc1, TEST_LOCATION)));
		assertThat(type.getDescription(), is(Optional.of("description")));

		var field = type.getField("f1").get();
		assertThat(field.getDefinedAt(), is(loc2));
	}

	@Test
	public void testEditField()
	{
		var loc1 = Location.create("LOC1");
		var loc2 = Location.create("LOC2");

		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.withDefinedAt(loc1)
				.addField(FieldDef.create("f1")
					.withDefinedAt(loc2)
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addTypeDefProcessor(new TypeDefProcessor<InterfaceDef>()
			{
				@Override
				public Location getLocation()
				{
					return TEST_LOCATION;
				}

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
					var field = type.getField("f1").get();
					encounter.edit(field, builder -> builder.withType(ScalarDef.INT));
				}
			})
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InterfaceDef.class).get();
		assertThat(type.getDefinedAt(), is(MergedLocation.of(loc1, TEST_LOCATION)));

		var field = type.getField("f1").get();
		assertThat(field.getType(), is(ScalarDef.INT));
		assertThat(field.getDefinedAt(), is(MergedLocation.of(loc2, TEST_LOCATION)));
	}

	@Test
	public void testEditArgument()
	{
		var loc1 = Location.create("LOC1");
		var loc2 = Location.create("LOC2");
		var loc3 = Location.create("LOC3");

		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.withDefinedAt(loc1)
				.addField(FieldDef.create("f1")
					.withDefinedAt(loc2)
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withDefinedAt(loc3)
						.withType(ScalarDef.INT)
						.addDirective(DirectiveUse.create("stringActually").build())
						.build()
					)
					.build()
				)
				.build()
			)
			.addDirectiveUseProcessor(new DirectiveUseProcessor<ArgumentDef>()
			{
				@Override
				public Location getLocation()
				{
					return TEST_LOCATION;
				}

				@Override
				public String getName()
				{
					return "stringActually";
				}

				@Override
				public Class<ArgumentDef> getContextType()
				{
					return ArgumentDef.class;
				}

				@Override
				public void process(
					ProcessingEncounter encounter,
					ArgumentDef location,
					DirectiveUse directive
				)
				{
					encounter.edit(location, builder -> builder.withType(ScalarDef.STRING));
				}
			})
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InterfaceDef.class).get();
		assertThat(type.getDefinedAt(), is(MergedLocation.of(loc1, TEST_LOCATION)));

		var field = type.getField("f1").get();
		assertThat(field.getType(), is(ScalarDef.STRING));
		assertThat(field.getDefinedAt(), is(MergedLocation.of(loc2, TEST_LOCATION)));

		var arg = field.getArgument("a1").get();
		assertThat(arg.getType(), is(ScalarDef.STRING));
		assertThat(arg.getDefinedAt(), is(MergedLocation.of(loc3, TEST_LOCATION)));
	}

	@Test
	public void testEditInputField()
	{
		var loc1 = Location.create("LOC1");
		var loc2 = Location.create("LOC2");

		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.withDefinedAt(loc1)
				.addField(InputFieldDef.create("f1")
					.withDefinedAt(loc2)
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addTypeDefProcessor(new TypeDefProcessor<InputObjectDef>()
			{
				@Override
				public Location getLocation()
				{
					return TEST_LOCATION;
				}

				@Override
				public Class<InputObjectDef> getType()
				{
					return InputObjectDef.class;
				}

				@Override
				public void process(
					ProcessingEncounter encounter,
					InputObjectDef type
				)
				{
					var field = type.getField("f1").get();
					encounter.edit(field, builder -> builder.withType(ScalarDef.INT));
				}
			})
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", InputObjectDef.class).get();
		assertThat(type.getDefinedAt(), is(MergedLocation.of(loc1, TEST_LOCATION)));

		var field = type.getField("f1").get();
		assertThat(field.getType(), is(ScalarDef.INT));
		assertThat(field.getDefinedAt(), is(MergedLocation.of(loc2, TEST_LOCATION)));
	}
}

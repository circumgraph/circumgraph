package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.validation.ValidationMessage;

import org.junit.jupiter.api.Test;

public class ModelBuildingTest
{
	@Test
	public void testEmpty()
	{
		var model = Model.create()
			.build();

		assertThat(model.get("Boolean").get(), is(ScalarDef.BOOLEAN));
		assertThat(model.get("Float").get(), is(ScalarDef.FLOAT));
		assertThat(model.get("Int").get(), is(ScalarDef.INT));
		assertThat(model.get("String").get(), is(ScalarDef.STRING));
	}

	@Test
	public void testEmptyObject()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));
	}

	@Test
	public void testEmptyInterface()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("Test")
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Test"));
	}

	@Test
	public void testObjectImplements()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("I")
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addImplements("I")
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		var structured = (StructuredDef) t;
		assertThat(structured.getImplementsNames(), contains("I"));

		assertThat(model.getImplements("I"), contains(t));
	}

	@Test
	public void testObjectIndirectlyImplements()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("I1")
				.build()
			)
			.addType(InterfaceDef.create("I2")
				.addImplements("I1")
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addImplements("I2")
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		var i2 = model.get("I2").get();

		var structured = (StructuredDef) t;
		assertThat(structured.getImplementsNames(), contains("I2"));

		assertThat(model.getImplements("I1"), contains(i2));
		assertThat(model.getImplements("I2"), contains(t));

		assertThat(model.findImplements("I1"), containsInAnyOrder(t, i2));
	}

	@Test
	public void testObjectScalarField()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("t1")
					.withType("Int")
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		var s = (StructuredDef) t;
		var f1 = s.getField("t1").get();

		assertThat(f1.getName(), is("t1"));
		assertThat(f1.getType(), is(ScalarDef.INT));
	}

	@Test
	public void testMergeSimple()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f2")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		var def = StructuredDef.class.cast(t);
		assertThat(def.getFields(), contains(
			FieldDef.create("f1")
				.withType(ScalarDef.STRING)
				.build(),
			FieldDef.create("f2")
				.withType(ScalarDef.STRING)
				.build()
		));
	}

	@Test
	public void testMergeField()
	{
		var schema = Schema.create()
			.addDirectiveUseProcessor(new TestDirectiveOnFieldProcessor())
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addDirective(DirectiveUse.create("test")
						.build())
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		var def = StructuredDef.class.cast(t);
		assertThat(def.getFields(), contains(
			FieldDef.create("f1")
				.withType(ScalarDef.STRING)
				.addDirective(DirectiveUse.create("test")
					.build())
				.build()
		));
	}

	@Test
	public void testMergeFieldDifferentTypes()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.INT)
					.build()
				)
				.build()
			)
			.build();

		assertThrows(ModelException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});
	}

	@Test
	public void testInvalidInterfaceLoop()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("A")
				.addImplements("B")
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addImplements("A")
				.build()
			)
			.build();

		assertThrows(ModelException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});
	}

	@Test
	public void testInvalidInterfaceLoopIndirect()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("A")
				.addImplements("B")
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addImplements("C")
				.build()
			)
			.addType(InterfaceDef.create("C")
				.addImplements("A")
				.build()
			)
			.build();

		assertThrows(ModelException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});
	}

	@Test
	public void testInvalidImplements()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addImplements("A")
				.build()
			)
			.build();

		assertThrows(ModelException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});
	}

	@Test
	public void testImplementsFieldSameTypes()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		Model.create()
			.addSchema(schema)
			.build();
	}

	@Test
	public void testImplementsFieldCompatibleTypes()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType("A")
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType("B")
					.build()
				)
				.build()
			)
			.build();

		Model.create()
			.addSchema(schema)
			.build();
	}

	@Test
	public void testImplementsFieldSameTypesInList()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output(ScalarDef.STRING))
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output(ScalarDef.STRING))
					.build()
				)
				.build()
			)
			.build();

		Model.create()
			.addSchema(schema)
			.build();
	}

	@Test
	public void testImplementsFieldCompatibleTypesInList()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output("A"))
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output("B"))
					.build()
				)
				.build()
			)
			.build();

		Model.create()
			.addSchema(schema)
			.build();
	}

	@Test
	public void testImplementsFieldDifferentTypes()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addImplements("B")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("B")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.INT)
					.build()
				)
				.build()
			)
			.build();

		assertThrows(ModelException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});
	}


	static class TestDirectiveOnFieldProcessor
		implements DirectiveUseProcessor<FieldDef>
	{
		@Override
		public String getName()
		{
			return "test";
		}

		@Override
		public Class<FieldDef> getContextType()
		{
			return FieldDef.class;
		}

		@Override
		public void process(
			FieldDef location,
			DirectiveUse directive,
			Consumer<ValidationMessage> validationCollector
		)
		{
			// Always valid
		}
	}
}

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
import com.circumgraph.model.validation.ValidationMessageLevel;

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

		var i = model.get("I", InterfaceDef.class).get();
		assertThat(i.getImplementors(), contains(t));
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

		var i1 = model.get("I1", InterfaceDef.class).get();
		var i2 = model.get("I2", InterfaceDef.class).get();

		var structured = (StructuredDef) t;
		assertThat(structured.getImplementsNames(), contains("I2"));

		assertThat(i1.getImplementors(), contains(i2));
		assertThat(i2.getImplementors(), contains(t));

		assertThat(i1.getAllImplementors(), containsInAnyOrder(t, i2));
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
	public void testFieldInvalidType()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType("Unknown")
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testFieldInvalidTypeInNonNull()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(NonNullDef.output("Unknown"))
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testFieldInvalidTypeInList()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output("Unknown"))
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testFieldInvalidTypeInListNonNull()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ListDef.output(NonNullDef.output("Unknown")))
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testFieldArgumentInvalidType()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("a1")
						.withType("Unknown")
						.build()
					)
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:argument:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("argument"), is("a1"));
		assertThat(msg.getArguments().get("argumentType"), is("Unknown"));
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

	@Test
	public void testMergeUnion()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(ObjectDef.create("B")
				.addField(FieldDef.create("f2")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(UnionDef.create("U")
				.addType(TypeRef.create("A"))
				.build()
			)
			.addType(UnionDef.create("U")
				.addType(TypeRef.create("B"))
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("U", UnionDef.class).get();
		assertThat(t.getName(), is("U"));

		assertThat(t.getTypeNames(), contains("A", "B"));
	}

	@Test
	public void testMergeEnum()
	{
		var schema = Schema.create()
			.addType(EnumDef.create("Direction")
				.addValue(EnumValueDef.create("NORTH")
					.build())
				.addValue(EnumValueDef.create("EAST")
					.build())
				.build()
			)
			.addType(EnumDef.create("Direction")
				.addValue(EnumValueDef.create("SOUTH")
					.build())
				.addValue(EnumValueDef.create("WEST")
					.build())
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Direction", EnumDef.class).get();
		assertThat(
			t.getValues().collect(EnumValueDef::getName),
			containsInAnyOrder("NORTH", "EAST", "SOUTH", "WEST")
		);
	}

	@Test
	public void testInputObjectScalarField()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("t1")
					.withType("Int")
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test", InputObjectDef.class).get();
		assertThat(t.getName(), is("Test"));

		var f1 = t.getField("t1").get();
		assertThat(f1.getName(), is("t1"));
		assertThat(f1.getType(), is(ScalarDef.INT));
	}

	@Test
	public void testInputFieldInvalidType()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withType("Unknown")
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testInputFieldInvalidTypeInNonNull()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withType(NonNullDef.input("Unknown"))
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testInputFieldInvalidTypeInList()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withType(ListDef.input("Unknown"))
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
	}

	@Test
	public void testInputFieldInvalidTypeInListNonNull()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withType(ListDef.input(NonNullDef.input("Unknown")))
					.build()
				)
				.build()
			)
			.build();

		var m = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = m.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:field:type-unknown"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("fieldType"), is("Unknown"));
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

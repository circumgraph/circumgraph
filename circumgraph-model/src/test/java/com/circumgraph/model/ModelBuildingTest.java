package com.circumgraph.model;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.model.validation.ValidationMessageCollector;

import org.junit.jupiter.api.Test;

public class ModelBuildingTest
{
	@Test
	public void testEmpty()
	{
		Model model = Model.create()
			.build();

		assertThat(model.get("Boolean").get(), is(ScalarDef.BOOLEAN));
		assertThat(model.get("Float").get(), is(ScalarDef.FLOAT));
		assertThat(model.get("Int").get(), is(ScalarDef.INT));
		assertThat(model.get("String").get(), is(ScalarDef.STRING));
	}

	@Test
	public void testEmptyObject()
	{
		Model model = Model.create()
			.addType(ObjectDef.create("Test")
				.build()
			)
			.build();

		TypeDef t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));
	}

	@Test
	public void testEmptyInterface()
	{
		Model model = Model.create()
			.addType(InterfaceDef.create("Test")
				.build()
			)
			.build();

		TypeDef t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Test"));
	}

	@Test
	public void testObjectImplements()
	{
		Model model = Model.create()
			.addType(InterfaceDef.create("I")
				.build()
			)
			.addType(ObjectDef.create("Test")
				.addImplements("I")
				.build()
			)
			.build();

		TypeDef t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		StructuredDef structured = (StructuredDef) t;
		assertThat(structured.getImplementsNames(), contains("I"));

		assertThat(model.getImplements("I"), contains(t));
	}

	@Test
	public void testObjectIndirectlyImplements()
	{
		Model model = Model.create()
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

		var t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		var i2 = model.get("I2").get();

		StructuredDef structured = (StructuredDef) t;
		assertThat(structured.getImplementsNames(), contains("I2"));

		assertThat(model.getImplements("I1"), contains(i2));
		assertThat(model.getImplements("I2"), contains(t));

		assertThat(model.findImplements("I1"), containsInAnyOrder(t, i2));
	}

	@Test
	public void testObjectScalarField()
	{
		Model model = Model.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("t1")
					.withType("Int")
					.build()
				)
				.build()
			)
			.build();

		TypeDef t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		StructuredDef s = (StructuredDef) t;
		FieldDef f1 = s.getField("t1").get();

		assertThat(f1.getName(), is("t1"));
		assertThat(f1.getType(), is(ScalarDef.INT));
	}

	@Test
	public void testMergeSimple()
	{
		Model model = Model.create()
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

		TypeDef t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		StructuredDef def = StructuredDef.class.cast(t);
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
		Model model = Model.create()
			.addDirectiveValidator(new TestDirectiveOnFieldValidator())
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

		TypeDef t = model.get("Test").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Test"));

		StructuredDef def = StructuredDef.class.cast(t);
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
		assertThrows(ModelException.class, () -> {
			Model.create()
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
		});
	}

	static class TestDirectiveOnFieldValidator
		implements DirectiveValidator<FieldDef>
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
		public void validate(
			FieldDef location,
			DirectiveUse directive,
			ValidationMessageCollector collector
		)
		{
			// Always valid
		}
	}
}

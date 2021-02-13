package com.circumgraph.model;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

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
}

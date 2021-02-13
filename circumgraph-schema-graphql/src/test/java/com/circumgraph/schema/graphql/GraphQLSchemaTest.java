package com.circumgraph.schema.graphql;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;

import org.junit.jupiter.api.Test;

public class GraphQLSchemaTest
{
	private static Model parse(String in)
	{
		return Model.create()
			.addSchema(GraphQLSchema.create(in))
			.build();
	}

	@Test
	public void testSingleInterface()
	{
		Model model = parse("interface Page {}");

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Page"));
	}

	@Test
	public void testInterfaceImplements()
	{
		Model model = parse("interface I {}\ninterface Page implements I {}");

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Page"));

		StructuredDef structured = (StructuredDef) t;
		assertThat(structured.getImplements(), contains(model.get("I").get()));
	}

	@Test
	public void testSingleObjectField()
	{
		Model model = parse("interface Page {\ntitle: String!\n}");

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Page"));

		StructuredDef s = (StructuredDef) t;

		FieldDef f1 = s.getField("title").get();
		assertThat(f1.getName(), is("title"));
		assertThat(f1.getType(), is(ScalarDef.STRING));
	}

	@Test
	public void testDirectiveOnType()
	{
		Model model = parse("interface Page @test {}");

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Page"));
	}

	@Test
	public void testInterfaceRedefine()
	{
		assertThrows(ModelException.class, () -> {
			parse(
				"interface Page {\nid: String\n}\n"
				+ "interface Page {\nvalue: Int\n}"
			);
		});
	}

	@Test
	public void testInterfaceExtend()
	{
		Model model = parse(
			"interface Page {\nid: String\n}\n"
			+ "extend interface Page {\nvalue: Int\n}"
		);

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Page"));
	}

	@Test
	public void testEnum()
	{
		Model model = parse("enum Direction {\nNORTH\nEAST\nSOUTH\nWEST\n}");

		TypeDef t = model.get("Direction").get();
		assertThat(t, instanceOf(EnumDef.class));
		assertThat(t.getName(), is("Direction"));

		EnumDef e = (EnumDef) t;
		assertThat(
			e.getValues().collect(EnumValueDef::getName),
			contains("NORTH", "EAST", "SOUTH", "WEST")
		);
	}
}

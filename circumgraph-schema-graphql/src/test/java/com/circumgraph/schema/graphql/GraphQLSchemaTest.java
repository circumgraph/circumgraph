package com.circumgraph.schema.graphql;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.HasDirectives;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.model.validation.ValidationMessage;

import org.junit.jupiter.api.Test;

public class GraphQLSchemaTest
{
	private static Model parse(String in)
	{
		return Model.create()
			.addDirectiveValidator(new TestDirectiveValidator())
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
	public void testInterfaceDirective()
	{
		Model model = parse(
			"interface Page @test(v: 100) {}\n"
		);

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Page"));

		StructuredDef def = StructuredDef.class.cast(t);
		assertThat(def.getDirectives(), contains(
			DirectiveUse.create("test")
				.addArgument("v", 100)
				.build()
		));
	}

	@Test
	public void testObjectDirective()
	{
		Model model = parse(
			"type Page @test(v: 100) {}\n"
		);

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Page"));

		StructuredDef def = StructuredDef.class.cast(t);
		assertThat(def.getDirectives(), contains(
			DirectiveUse.create("test")
				.addArgument("v", 100)
				.build()
		));
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

	@Test
	public void testUnion()
	{
		Model model = parse(
			"""
				type A { id: String }
				type B { title: Int }

				union U = A | B
			"""
		);

		TypeDef t = model.get("U").get();
		assertThat(t, instanceOf(UnionDef.class));
		assertThat(t.getName(), is("U"));

		UnionDef def = UnionDef.class.cast(t);
		assertThat(
			def.getTypeNames(),
			contains("A", "B")
		);
	}

	@Test
	public void testObjectFieldNonNull()
	{
		Model model = parse("type Page {\ntitle: String!\n}");

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Page"));

		StructuredDef s = (StructuredDef) t;

		FieldDef f1 = s.getField("title").get();
		assertThat(f1.getName(), is("title"));
		assertThat(f1.getType(), is(ScalarDef.STRING));
		assertThat(f1.isNullable(), is(false));
	}

	@Test
	public void testObjectFieldNullable()
	{
		Model model = parse("type Page {\ntitle: String\n}");

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Page"));

		StructuredDef s = (StructuredDef) t;

		FieldDef f1 = s.getField("title").get();
		assertThat(f1.getName(), is("title"));
		assertThat(f1.getType(), is(ScalarDef.STRING));
		assertThat(f1.isNullable(), is(true));
	}

	@Test
	public void testInterfaceFieldDirective()
	{
		Model model = parse(
			"interface Page {\nfield: String! @test(v: 100)\n}\n"
		);

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Page"));

		StructuredDef def = StructuredDef.class.cast(t);

		FieldDef f1 = def.getField("field").get();
		assertThat(f1.getName(), is("field"));
		assertThat(f1.getType(), is(ScalarDef.STRING));
		assertThat(f1.isNullable(), is(false));
		assertThat(f1.getDirectives(), contains(
			DirectiveUse.create("test")
				.addArgument("v", 100)
				.build()
		));
	}

	static class TestDirectiveValidator
		implements DirectiveValidator<HasDirectives>
	{
		@Override
		public String getName()
		{
			return "test";
		}

		@Override
		public Class<HasDirectives> getContextType()
		{
			return HasDirectives.class;
		}

		@Override
		public void validate(
			HasDirectives location,
			DirectiveUse directive,
			Consumer<ValidationMessage> validationCollector
		)
		{
			// Always valid
		}
	}
}

package com.circumgraph.schema.graphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.HasDirectives;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.ProcessingEncounter;

import org.junit.jupiter.api.Test;

public class GraphQLSchemaTest
{
	private static Model parse(String in)
	{
		return Model.create()
			.addSchema(Schema.create()
				.addDirectiveUseProcessor(new TestDirectiveProcessor())
				.build()
			)
			.addSchema(GraphQLSchema.create(TextSource.create("<test>", in)))
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
		var model = parse("""
			interface Page {
				id: String
			}

			interface Page {
				value: Int
			}
		""");

		var t = model.get("Page", StructuredDef.class).get();
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Page"));

		assertThat(t.getField("id").get().getType(), is(ScalarDef.STRING));
		assertThat(t.getField("value").get().getType(), is(ScalarDef.INT));
	}

	@Test
	public void testInterfaceExtend()
	{
		Model model = parse("""
			interface Page {
				id: String
			}

			extend interface Page {
				value: Int
			}
		""");

		var t = model.get("Page", StructuredDef.class).get();
		assertThat(t, instanceOf(InterfaceDef.class));
		assertThat(t.getName(), is("Page"));

		assertThat(t.getField("id").get().getType(), is(ScalarDef.STRING));
		assertThat(t.getField("value").get().getType(), is(ScalarDef.INT));
	}

	@Test
	public void testEnum()
	{
		Model model = parse("""
			enum Direction {
				NORTH
				EAST
				SOUTH
				WEST
			}
		""");

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
	public void testEnumMerge()
	{
		Model model = parse("""
			enum Direction {
				NORTH
				EAST
			}

			extend enum Direction {
				SOUTH
				WEST
			}
		""");

		TypeDef t = model.get("Direction").get();
		assertThat(t, instanceOf(EnumDef.class));
		assertThat(t.getName(), is("Direction"));

		EnumDef e = (EnumDef) t;
		assertThat(
			e.getValues().collect(EnumValueDef::getName),
			containsInAnyOrder("NORTH", "EAST", "SOUTH", "WEST")
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
	public void testUnionExtend()
	{
		Model model = parse(
			"""
				type A { id: String }
				type B { title: Int }

				union U = A

				extend union U = B
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
	public void testObjectExtend()
	{
		Model model = parse("""
			type Page {
				id: String
			}

			extend type Page {
				value: Int
			}
		""");

		var t = model.get("Page", StructuredDef.class).get();
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Page"));

		assertThat(t.getField("id").get().getType(), is(ScalarDef.STRING));
		assertThat(t.getField("value").get().getType(), is(ScalarDef.INT));
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
		assertThat(f1.getType(), is(NonNullDef.output("String")));
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
	}

	@Test
	public void testObjectFieldDirective()
	{
		Model model = parse("type Page {\ntitle: String! @test\n}");

		TypeDef t = model.get("Page").get();
		assertThat(t, instanceOf(StructuredDef.class));
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Page"));

		StructuredDef s = (StructuredDef) t;

		FieldDef f1 = s.getField("title").get();
		assertThat(f1.getName(), is("title"));
		assertThat(f1.getType(), is(NonNullDef.output("String")));
		assertThat(f1.getDirectives(), contains(
			DirectiveUse.create("test")
				.build()
		));
	}


	@Test
	public void testObjectFieldList()
	{
		Model model = parse(
			"""
				type Book { authors: [String!]! }
			"""
		);

		TypeDef t = model.get("Book").get();
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Book"));

		StructuredDef s = (StructuredDef) t;

		FieldDef f1 = s.getField("authors").get();
		assertThat(f1.getName(), is("authors"));
		assertThat(f1.getType(), is(
			NonNullDef.output(
				ListDef.output(
					NonNullDef.output("String")
				)
			)
		));
	}

	@Test
	public void testObjectFieldListDirective()
	{
		Model model = parse(
			"""
				type Book { authors: [String]! @test }
			"""
		);

		TypeDef t = model.get("Book").get();
		assertThat(t, instanceOf(ObjectDef.class));
		assertThat(t.getName(), is("Book"));

		StructuredDef s = (StructuredDef) t;

		FieldDef f1 = s.getField("authors").get();
		assertThat(f1.getName(), is("authors"));
		assertThat(f1.getType(), is(
			NonNullDef.output(
				ListDef.output(TypeRef.create("String"))
			)
		));
		assertThat(f1.getDirectives(), contains(
			DirectiveUse.create("test")
				.build()
		));
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
		assertThat(f1.getType(), is(NonNullDef.output("String")));
		assertThat(f1.getDirectives(), contains(
			DirectiveUse.create("test")
				.addArgument("v", 100)
				.build()
		));
	}

	@Test
	public void testInvalidInterfaceLoop()
	{
		assertThrows(ModelException.class, () -> {
			parse(
				"""
					interface A implements B { id: String }
					interface B implements A { title: Int }
				"""
			);
		});
	}

	static class TestDirectiveProcessor
		implements DirectiveUseProcessor<HasDirectives>
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
		public void process(
			ProcessingEncounter encounter,
			HasDirectives location,
			DirectiveUse directive
		)
		{
			// Always valid
		}
	}
}

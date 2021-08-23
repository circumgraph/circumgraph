package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.internal.UnionDefImpl;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class UnionDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(UnionDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs"
			)
			.verify();
	}

	@Test
	public void testCreate()
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
	public void testCreateUnknownType()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(UnionDef.create("U")
				.addType(TypeRef.create("A"))
				.addType(TypeRef.create("B"))
				.build()
			)
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:union:unknown-type"));
		assertThat(msg.getArguments().get("type"), is("U"));
		assertThat(msg.getArguments().get("subType"), is("B"));
		assertThat(msg.getMessage(), is("`B` is part of union `U`, but type is not declared"));
	}

	@Test
	public void testCreateInvalidType()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(UnionDef.create("U")
				.addType(TypeRef.create("A"))
				.addType(ListDef.output("A"))
				.build()
			)
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create()
				.addSchema(schema)
				.build();
		});

		var msg = e.getIssues().getFirst();
		assertThat(msg.getLevel(), is(ValidationMessageLevel.ERROR));
		assertThat(msg.getCode(), is("model:union:object-type-required"));
		assertThat(msg.getArguments().get("type"), is("U"));
		assertThat(msg.getArguments().get("subType"), is("[A]"));
		assertThat(msg.getMessage(), is("`[A]` is part of union `U`, but is not an object type"));
	}

	@Test
	public void testMerge()
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
}

package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import com.circumgraph.model.internal.InterfaceDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class InterfaceDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(InterfaceDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs",
				"metadata",
				"fields"
			)
			.verify();
	}

	@Test
	public void testDerive()
	{
		var i1 = InterfaceDef.create("name")
			.withDescription("description")
			.addField(FieldDef.create("test")
				.withType(ScalarDef.STRING)
				.build()
			)
			.build();

		var i2 = InterfaceDef.create("name")
			.withDescription("updated description")
			.addField(FieldDef.create("test")
				.withType(ScalarDef.STRING)
				.build()
			)
			.build();

		var d = i1.derive()
			.withDescription("updated description")
			.build();

		assertThat(d, is(i2));
	}

	@Test
	public void testDirectImplements()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("I1")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("T")
				.addImplements("I1")
				.build()
			)
			.build();

		var model = Model.create().addSchema(schema).build();

		var t = model.get("T", StructuredDef.class).get();
		assertThat(t.hasImplements("I1"), is(true));
		assertThat(t.findImplements("I1"), is(true));

		var i1 = model.get("I1").get();
		assertThat(t.getImplements(), contains(i1));
		assertThat(t.getAllImplements(), contains(i1));
	}

	@Test
	public void testIndirectImplements()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("I1")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("I2")
				.addImplements("I1")
				.build()
			)
			.addType(InterfaceDef.create("T")
				.addImplements("I2")
				.build()
			)
			.build();

		var model = Model.create().addSchema(schema).build();

		var t = model.get("T", StructuredDef.class).get();

		assertThat(t.hasImplements("I2"), is(true));
		assertThat(t.findImplements("I2"), is(true));
		assertThat(t.hasImplements("I1"), is(false));
		assertThat(t.findImplements("I1"), is(true));

		var i1 = model.get("I1").get();
		var i2 = model.get("I2").get();
		assertThat(t.getAllImplements(), containsInAnyOrder(i1, i2));
	}

	@Test
	public void testDirectField()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("T")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create().addSchema(schema).build();

		var t = model.get("T", StructuredDef.class).get();
		var field = t.getField("f1").get();
		assertThat(field.getName(), is("f1"));
		assertThat(field.getType(), is(ScalarDef.STRING));
	}

	@Test
	public void testIndirectField()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("I1")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("T")
				.addImplements("I1")
				.build()
			)
			.build();

		var model = Model.create().addSchema(schema).build();

		var t = model.get("T", StructuredDef.class).get();
		var field = t.getField("f1").get();
		assertThat(field.getName(), is("f1"));
		assertThat(field.getType(), is(ScalarDef.STRING));
	}

	@Test
	public void testIndirectFieldRedefined()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("I1")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("T")
				.addImplements("I1")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.withDescription("description")
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create().addSchema(schema).build();

		var t = model.get("T", StructuredDef.class).get();
		var field = t.getField("f1").get();
		assertThat(field.getName(), is("f1"));
		assertThat(field.getType(), is(ScalarDef.STRING));
		assertThat(field.getDescription(), is(Optional.of("description")));
	}

	@Test
	public void testIndirectFieldRedefinedIncompatible()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("I1")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("T")
				.addImplements("I1")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.INT)
					.build()
				)
				.build()
			)
			.build();

		var e = assertThrows(ModelValidationException.class, () -> {
			Model.create().addSchema(schema).build();
		});

		var error = e.getIssues().getFirst();
		assertThat(error.getCode(), is("model:incompatible-interface-field-type"));

		var args = error.getArguments();
		assertThat(args.get("type"), is("T"));
		assertThat(args.get("field"), is("f1"));
		assertThat(args.get("fieldType"), is("Int"));
		assertThat(args.get("interface"), is("I1"));
		assertThat(args.get("interfaceFieldType"), is("String"));
	}

	@Test
	public void testDirectImplementors()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("I1")
				.addImplements("T")
				.build()
			)
			.addType(InterfaceDef.create("T")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create().addSchema(schema).build();

		var t = model.get("T", InterfaceDef.class).get();

		var i1 = model.get("I1").get();
		assertThat(t.getImplementors(), contains(i1));
		assertThat(t.getAllImplementors(), contains(i1));
	}

	@Test
	public void testIndirectImplementors()
	{
		var schema = Schema.create()
			.addType(InterfaceDef.create("I1")
				.addImplements("T")
				.build()
			)
			.addType(InterfaceDef.create("I2")
				.addImplements("I1")
				.build()
			)
			.addType(InterfaceDef.create("T")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create().addSchema(schema).build();

		var t = model.get("T", InterfaceDef.class).get();

		var i1 = model.get("I1").get();
		var i2 = model.get("I2").get();
		assertThat(t.getImplementors(), contains(i1));
		assertThat(t.getAllImplementors(), containsInAnyOrder(i1, i2));
	}
}

package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import com.circumgraph.model.internal.ObjectDefImpl;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ObjectDefTest
{
	private static MetadataKey<String> KEY = MetadataKey.create("key", String.class);

	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(ObjectDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs",
				"fields"
			)
			.verify();
	}

	@Test
	public void testEmpty()
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
	public void testWithMetadata()
	{
		var t = ObjectDef.create("name")
			.withDescription("description")
			.addField(FieldDef.create("test")
				.withType(ScalarDef.STRING)
				.build()
			)
			.withMetadata(KEY, "value1")
			.build();

		assertThat(t.getMetadata(KEY), is(Optional.of("value1")));
	}

	@Test
	public void testDerive()
	{
		var o1 = ObjectDef.create("name")
			.withDescription("description")
			.addField(FieldDef.create("test")
				.withType(ScalarDef.STRING)
				.build()
			)
			.withMetadata(KEY, "value1")
			.build();

		var o2 = ObjectDef.create("name")
			.withDescription("updated description")
			.addField(FieldDef.create("test")
				.withType(ScalarDef.STRING)
				.build()
			)
			.withMetadata(KEY, "value1")
			.build();

		var d = o1.derive()
			.withDescription("updated description")
			.build();

		assertThat(d, is(o2));
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
			.addType(ObjectDef.create("T")
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
			.addType(ObjectDef.create("T")
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
	public void testInvalidImplements()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("A")
				.build()
			)
			.addType(ObjectDef.create("B")
				.addImplements("A")
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
		assertThat(msg.getCode(), is("model:invalid-implements"));
		assertThat(msg.getArguments().get("type"), is("B"));
		assertThat(msg.getArguments().get("implements"), is("A"));
		assertThat(msg.getMessage(), is("`B` can not implement `A`, type is not an interface"));
	}

	@Test
	public void testDirectField()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("T")
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
			.addType(ObjectDef.create("T")
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
			.addType(ObjectDef.create("T")
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
			.addType(ObjectDef.create("T")
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
	public void testMerge()
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
	public void testMergeInvalid()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.withDefinedAt(Location.create("LOC1"))
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InterfaceDef.create("Test")
				.withDefinedAt(Location.create("LOC2"))
				.addField(FieldDef.create("f2")
					.withType(ScalarDef.STRING)
					.build()
				)
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
		assertThat(msg.getCode(), is("model:incompatible-type"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("originalLocation"), is("LOC1"));
		assertThat(msg.getMessage(), is("Could not merge: `Test` has a different type than previously defined at LOC1"));
	}
}

package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.internal.InputFieldDefImpl;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class InputFieldDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(InputFieldDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs",
				"declaringType"
			)
			.verify();
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

	@Test
	public void testReferencedTypeHoisted()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("A")
				.addField(InputFieldDef.create("f1")
					.withType(InputObjectDef.create("B")
						.addField(InputFieldDef.create("f2")
							.withType(ScalarDef.INT)
							.build()
						)
						.build()
					)
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var b = model.get("B", InputObjectDef.class);
		assertThat(b.isPresent(), is(true));
	}

	@Test
	public void testMerge()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.withDescription("description")
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test").get();
		assertThat(t, instanceOf(InputObjectDef.class));
		assertThat(t.getName(), is("Test"));

		var def = InputObjectDef.class.cast(t);
		assertThat(def.getFields(), contains(
			InputFieldDef.create("f1")
				.withType(ScalarDef.STRING)
				.withDescription("description")
				.build()
		));
	}

	@Test
	public void testMergeInvalid()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withDefinedAt(Location.create("LOC1"))
					.withType(ScalarDef.STRING)
					.build()
				)
				.build()
			)
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("f1")
					.withDefinedAt(Location.create("LOC2"))
					.withType(ScalarDef.INT)
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
		assertThat(msg.getCode(), is("model:incompatible-field-type"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("originalLocation"), is("LOC2"));
	}
}

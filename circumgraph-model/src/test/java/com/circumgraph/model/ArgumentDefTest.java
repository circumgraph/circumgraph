package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.circumgraph.model.internal.ArgumentDefImpl;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ArgumentDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(ArgumentDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs",
				"declaringField"
			)
			.verify();
	}

	@Test
	public void testInvalidNameNull()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create(null)
						.withType(ScalarDef.STRING)
						.build()
					)
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
		assertThat(msg.getCode(), is("model:argument:invalid-name"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("argument"), nullValue());
		assertThat(msg.getMessage(), is("The name of argument `null` for field `f1` in `Test` is not valid, should match [a-zA-Z_][a-zA-Z0-9_]*"));
	}

	@Test
	public void testInvalidNameLeadingDigit()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(ScalarDef.STRING)
					.addArgument(ArgumentDef.create("1abc")
						.withType(ScalarDef.STRING)
						.build()
					)
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
		assertThat(msg.getCode(), is("model:argument:invalid-name"));
		assertThat(msg.getArguments().get("type"), is("Test"));
		assertThat(msg.getArguments().get("field"), is("f1"));
		assertThat(msg.getArguments().get("argument"), is("1abc"));
		assertThat(msg.getMessage(), is("The name of argument `1abc` for field `f1` in `Test` is not valid, should match [a-zA-Z_][a-zA-Z0-9_]*"));
	}
}

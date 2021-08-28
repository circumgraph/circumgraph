package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import com.circumgraph.model.internal.NonNullDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class NonNullDefTest
{
	@Test
	public void testEqualityInput()
	{
		EqualsVerifier.forClass(NonNullDefImpl.InputImpl.class)
			.usingGetClass()
			.withIgnoredFields("defs")
			.verify();
	}

	@Test
	public void testEqualityOutput()
	{
		EqualsVerifier.forClass(NonNullDefImpl.OutputImpl.class)
			.usingGetClass()
			.withIgnoredFields("defs")
			.verify();
	}

	@Test
	public void testName()
	{
		var t = NonNullDef.output(ScalarDef.STRING);
		assertThat(t.getName(), is("String!"));
	}

	@Test
	public void testDescriptionEmpty()
	{
		var t = NonNullDef.output(ScalarDef.STRING);
		assertThat(t.getDescription(), is(Optional.empty()));
	}

	@Test
	public void testAssignableFromNonNull()
	{
		assertThat(
			NonNullDef.output(ScalarDef.STRING).isAssignableFrom(NonNullDef.output(ScalarDef.STRING)),
			is(true)
		);
	}

	@Test
	public void testAssignableFromNullable()
	{
		assertThat(
			NonNullDef.output(ScalarDef.STRING).isAssignableFrom(ScalarDef.STRING),
			is(true)
		);
	}

	@Test
	public void testNonAssignableFromDifferentNonNull()
	{
		assertThat(
			NonNullDef.output(ScalarDef.STRING).isAssignableFrom(NonNullDef.output(ScalarDef.INT)),
			is(false)
		);
	}

	@Test
	public void testNonAssignableFromDifferentNullable()
	{
		assertThat(
			NonNullDef.output(ScalarDef.STRING).isAssignableFrom(ScalarDef.INT),
			is(false)
		);
	}

	@Test
	public void testInputNotAssignableToOutput()
	{
		assertThat(
			NonNullDef.input(ScalarDef.STRING).isAssignableFrom(NonNullDef.output(ScalarDef.STRING)),
			is(false)
		);
	}

	@Test
	public void testModelResolvesTypes()
	{
		var schema = Schema.create()
			.addType(ObjectDef.create("Test")
				.addField(FieldDef.create("f1")
					.withType(NonNullDef.output("String"))
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var type = model.get("Test", StructuredDef.class).get();
		var field = type.getField("f1").get();
		var fieldType = (NonNullDef) field.getType();
		assertThat(fieldType.getType(), is(ScalarDef.STRING));
	}
}

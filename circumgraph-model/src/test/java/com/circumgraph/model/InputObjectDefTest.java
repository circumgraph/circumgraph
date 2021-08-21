package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.internal.InputObjectDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class InputObjectDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(InputObjectDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs"
			)
			.verify();
	}

	@Test
	public void testInputObjectScalarField()
	{
		var schema = Schema.create()
			.addType(InputObjectDef.create("Test")
				.addField(InputFieldDef.create("t1")
					.withType("Int")
					.build()
				)
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Test", InputObjectDef.class).get();
		assertThat(t.getName(), is("Test"));

		var f1 = t.getField("t1").get();
		assertThat(f1.getName(), is("t1"));
		assertThat(f1.getType(), is(ScalarDef.INT));
	}
}

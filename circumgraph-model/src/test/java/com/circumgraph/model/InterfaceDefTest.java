package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
}

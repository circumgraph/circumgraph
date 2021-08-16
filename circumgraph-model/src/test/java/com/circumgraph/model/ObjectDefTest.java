package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.internal.ObjectDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ObjectDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(ObjectDefImpl.class)
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
		var o1 = ObjectDef.create("name")
			.withDescription("description")
			.addField(FieldDef.create("test")
				.withType(ScalarDef.STRING)
				.build()
			)
			.build();

		var o2 = ObjectDef.create("name")
			.withDescription("updated description")
			.addField(FieldDef.create("test")
				.withType(ScalarDef.STRING)
				.build()
			)
			.build();

		var d = o1.derive()
			.withDescription("updated description")
			.build();

		assertThat(d, is(o2));
	}
}

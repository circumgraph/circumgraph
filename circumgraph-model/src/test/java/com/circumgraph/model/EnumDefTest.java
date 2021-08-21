package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.circumgraph.model.internal.EnumDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class EnumDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(EnumDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation"
			)
			.verify();
	}

	@Test
	public void testMerge()
	{
		var schema = Schema.create()
			.addType(EnumDef.create("Direction")
				.addValue(EnumValueDef.create("NORTH")
					.build())
				.addValue(EnumValueDef.create("EAST")
					.build())
				.build()
			)
			.addType(EnumDef.create("Direction")
				.addValue(EnumValueDef.create("SOUTH")
					.build())
				.addValue(EnumValueDef.create("WEST")
					.build())
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Direction", EnumDef.class).get();
		assertThat(
			t.getValues().collect(EnumValueDef::getName),
			containsInAnyOrder("NORTH", "EAST", "SOUTH", "WEST")
		);
	}

	@Test
	public void testMergeSameValue()
	{
		var schema = Schema.create()
			.addType(EnumDef.create("Direction")
				.addValue(EnumValueDef.create("NORTH")
					.build())
				.addValue(EnumValueDef.create("EAST")
					.build())
				.addValue(EnumValueDef.create("SOUTH")
					.build())
				.build()
			)
			.addType(EnumDef.create("Direction")
				.addValue(EnumValueDef.create("SOUTH")
					.build())
				.addValue(EnumValueDef.create("WEST")
					.build())
				.build()
			)
			.build();

		var model = Model.create()
			.addSchema(schema)
			.build();

		var t = model.get("Direction", EnumDef.class).get();
		assertThat(
			t.getValues().collect(EnumValueDef::getName),
			containsInAnyOrder("NORTH", "EAST", "SOUTH", "WEST")
		);
	}
}

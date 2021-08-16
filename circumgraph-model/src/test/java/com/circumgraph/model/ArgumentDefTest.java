package com.circumgraph.model;

import com.circumgraph.model.internal.ArgumentDefImpl;

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
				"defs"
			)
			.verify();
	}
}

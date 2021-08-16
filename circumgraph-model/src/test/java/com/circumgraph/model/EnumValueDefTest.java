package com.circumgraph.model;

import com.circumgraph.model.internal.EnumValueDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class EnumValueDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(EnumValueDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation"
			)
			.verify();
	}
}

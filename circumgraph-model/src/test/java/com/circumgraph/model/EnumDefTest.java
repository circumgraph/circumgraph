package com.circumgraph.model;

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
}

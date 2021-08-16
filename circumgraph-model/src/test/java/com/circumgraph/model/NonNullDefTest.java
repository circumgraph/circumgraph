package com.circumgraph.model;

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
}

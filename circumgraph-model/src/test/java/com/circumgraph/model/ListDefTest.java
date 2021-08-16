package com.circumgraph.model;

import com.circumgraph.model.internal.ListDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ListDefTest
{
	@Test
	public void testEqualityInput()
	{
		EqualsVerifier.forClass(ListDefImpl.InputImpl.class)
			.usingGetClass()
			.withIgnoredFields("defs")
			.verify();
	}

	@Test
	public void testEqualityOutput()
	{
		EqualsVerifier.forClass(ListDefImpl.OutputImpl.class)
			.usingGetClass()
			.withIgnoredFields("defs")
			.verify();
	}
}

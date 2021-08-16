package com.circumgraph.model;

import com.circumgraph.model.internal.ScalarDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ScalarDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(ScalarDefImpl.class)
			.usingGetClass()
			.verify();
	}
}

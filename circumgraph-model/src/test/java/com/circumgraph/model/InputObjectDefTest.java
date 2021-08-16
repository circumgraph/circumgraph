package com.circumgraph.model;

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
				"defs",
				"metadata"
			)
			.verify();
	}
}

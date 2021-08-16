package com.circumgraph.model;

import com.circumgraph.model.internal.InputFieldDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class InputFieldDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(InputFieldDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs",
				"metadata"
			)
			.verify();
	}
}

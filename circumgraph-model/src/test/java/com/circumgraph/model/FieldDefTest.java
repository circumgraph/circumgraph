package com.circumgraph.model;

import com.circumgraph.model.internal.FieldDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class FieldDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(FieldDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs",
				"metadata",
				"declaringType"
			)
			.verify();
	}
}

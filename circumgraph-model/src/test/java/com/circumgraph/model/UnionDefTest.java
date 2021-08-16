package com.circumgraph.model;

import com.circumgraph.model.internal.UnionDefImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class UnionDefTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(UnionDefImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation",
				"defs"
			)
			.verify();
	}
}

package com.circumgraph.model;

import com.circumgraph.model.internal.DirectiveUseImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class DirectiveUseTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(DirectiveUseImpl.class)
			.usingGetClass()
			.withIgnoredFields(
				"sourceLocation"
			)
			.verify();
	}
}

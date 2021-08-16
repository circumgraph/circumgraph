package com.circumgraph.model;

import com.circumgraph.model.internal.ArgumentUseImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ArgumentUseTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(ArgumentUseImpl.class)
			.usingGetClass()
			.verify();
	}
}

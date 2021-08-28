package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

	@Test
	public void testAssignableSame()
	{
		assertThat(
			ScalarDef.STRING.isAssignableFrom(ScalarDef.STRING),
			is(true)
		);
	}

	@Test
	public void testAssignableDifferent()
	{
		assertThat(
			ScalarDef.STRING.isAssignableFrom(ScalarDef.INT),
			is(false)
		);
	}

	@Test
	public void testAssignableFromNonNull()
	{
		assertThat(
			ScalarDef.STRING.isAssignableFrom(NonNullDef.output(ScalarDef.STRING)),
			is(true)
		);
	}
}

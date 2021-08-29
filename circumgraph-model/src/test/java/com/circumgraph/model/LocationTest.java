package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.circumgraph.model.internal.LocationImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class LocationTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(LocationImpl.class)
			.usingGetClass()
			.verify();
	}

	@Test
	public void testCode()
	{
		// Simply test that something is generated
		var l = Location.code();
		assertThat(l.describe(), notNullValue());
	}

	@Test
	public void testAutomaticNoPicked()
	{
		var l = Location.automatic();
		assertThat(l.describe(), notNullValue());
	}

	@Test
	public void testAutomaticPicked()
	{
		var l = Location.automatic(Location.create("Hello"));
		assertThat(l.describe(), is("Hello"));
	}

	@Test
	public void testScoped()
	{
		try(var h = Location.scope(Location.create("Hello")))
		{
			var l = Location.automatic();
			assertThat(l.describe(), is("Hello"));
		}
	}

	@Test
	public void testScopedMultiple()
	{
		try(var h = Location.scope(Location.create("Hello")))
		{
			try(var h2 = Location.scope(Location.create("Hello2")))
			{
				var l = Location.automatic();
				assertThat(l.describe(), is("Hello2"));
			}

			var l = Location.automatic();
			assertThat(l.describe(), is("Hello"));
		}
	}
}

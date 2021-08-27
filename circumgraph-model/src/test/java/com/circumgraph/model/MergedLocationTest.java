package com.circumgraph.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import com.circumgraph.model.internal.MergedLocationImpl;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class MergedLocationTest
{
	@Test
	public void testEquality()
	{
		EqualsVerifier.forClass(MergedLocationImpl.class)
			.usingGetClass()
			.verify();
	}

	@Test
	public void testMergeInitial()
	{
		var l1 = Location.create("LOC1");
		var l2 = Location.create("LOC2");

		var merged = l1.mergeWith(l2);
		assertThat(merged.list(), contains(l1, l2));
		assertThat(merged.describe(), is("LOC1 modified by LOC2"));
	}

	@Test
	public void testMergeMultiple()
	{
		var l1 = Location.create("LOC1");
		var l2 = Location.create("LOC2");
		var l3 = Location.create("LOC3");

		var merged = l1.mergeWith(l2).mergeWith(l3);
		assertThat(merged.list(), contains(l1, l2, l3));
		assertThat(merged.describe(), is("LOC1 modified by LOC2 modified by LOC3"));
	}

	@Test
	public void testToListNonMerged()
	{
		var l1 = Location.create("LOC1");

		assertThat(MergedLocation.toList(l1), contains(l1));
	}

	@Test
	public void testToListMerged()
	{
		var l1 = Location.create("LOC1");
		var l2 = Location.create("LOC2");

		var merged = l1.mergeWith(l2);
		assertThat(MergedLocation.toList(merged), contains(l1, l2));
	}
}

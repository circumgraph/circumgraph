package com.circumgraph.graphql.internal.search;

import com.circumgraph.model.ScalarDef;

/**
 * Criteria for matching a {@code Int} type. Allows exact and range matching.
 */
public class IntCriteria
	extends RangeCriteria
{
	public IntCriteria()
	{
		super(ScalarDef.INT);
	}
}

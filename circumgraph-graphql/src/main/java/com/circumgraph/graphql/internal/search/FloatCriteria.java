package com.circumgraph.graphql.internal.search;

import com.circumgraph.model.ScalarDef;

/**
 * Criteria for matching a {@code Float} type. Allows exact and range matching.
 */
public class FloatCriteria
	extends RangeCriteria
{
	public FloatCriteria()
	{
		super(ScalarDef.FLOAT);
	}
}

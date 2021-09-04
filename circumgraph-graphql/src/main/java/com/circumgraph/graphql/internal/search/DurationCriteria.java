package com.circumgraph.graphql.internal.search;

import com.circumgraph.model.ScalarDef;

public class DurationCriteria
	extends RangeCriteria
{
	public DurationCriteria()
	{
		super(ScalarDef.DURATION);
	}
}

package com.circumgraph.graphql.internal.search;

import com.circumgraph.model.ScalarDef;

public class OffsetTimeCriteria
	extends RangeCriteria
{
	public OffsetTimeCriteria()
	{
		super(ScalarDef.OFFSET_TIME);
	}
}

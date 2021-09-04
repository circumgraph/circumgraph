package com.circumgraph.graphql.internal.search;

import com.circumgraph.model.ScalarDef;

public class OffsetDateTimeCriteria
	extends RangeCriteria
{
	public OffsetDateTimeCriteria()
	{
		super(ScalarDef.OFFSET_DATE_TIME);
	}
}

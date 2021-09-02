package com.circumgraph.graphql.internal.search;

import com.circumgraph.model.ScalarDef;

public class ZonedDateTimeCriteria
	extends RangeCriteria
{
	public ZonedDateTimeCriteria()
	{
		super(ScalarDef.ZONED_DATE_TIME);
	}
}

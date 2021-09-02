package com.circumgraph.graphql.internal.search;

import com.circumgraph.model.ScalarDef;

public class LocalDateTimeCriteria
	extends RangeCriteria
{
	public LocalDateTimeCriteria()
	{
		super(ScalarDef.LOCAL_DATE_TIME);
	}
}

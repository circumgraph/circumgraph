package com.circumgraph.graphql.internal.search;

import com.circumgraph.model.ScalarDef;

public class LocalDateCriteria
	extends RangeCriteria
{
	public LocalDateCriteria()
	{
		super(ScalarDef.LOCAL_DATE);
	}
}

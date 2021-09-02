package com.circumgraph.graphql.internal.search;

import com.circumgraph.model.ScalarDef;

public class LocalTimeCriteria
	extends RangeCriteria
{
	public LocalTimeCriteria()
	{
		super(ScalarDef.LOCAL_TIME);
	}
}

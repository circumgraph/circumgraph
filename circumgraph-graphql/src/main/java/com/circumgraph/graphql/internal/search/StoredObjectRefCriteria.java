package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.search.QueryPath;

import se.l4.silo.index.search.QueryClause;

public class StoredObjectRefCriteria
	implements Criteria
{
	private final StructuredDef def;
	private final IDCriteria criteria;

	public StoredObjectRefCriteria(StructuredDef def, IDCriteria criteria)
	{
		this.def = def;
		this.criteria = criteria;
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return def;
	}

	@Override
	public InputObjectDef getGraphQLType()
	{
		return criteria.getGraphQLType();
	}

	@Override
	public QueryClause toClause(Map<String, Object> data, QueryPath path)
	{
		return criteria.toClause(data, path);
	}
}

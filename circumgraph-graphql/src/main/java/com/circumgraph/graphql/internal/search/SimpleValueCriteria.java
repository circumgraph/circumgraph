package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.graphql.internal.InputUnions;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.search.QueryPath;

import se.l4.silo.index.AnyMatcher;
import se.l4.silo.index.Matcher;
import se.l4.silo.index.NullMatcher;
import se.l4.silo.index.search.QueryClause;

/**
 * Abstract implementation of a {@link Criteria} that applies to a
 * {@link SimpleValue}.
 */
public abstract class SimpleValueCriteria
	implements Criteria
{
	@Override
	public QueryClause toClause(
		Map<String, Object> data,
		QueryPath path
	)
	{
		InputUnions.validate(getGraphQLType(), data);

		var field = (QueryPath.Leaf) path;
		if(data.get("any") != null)
		{
			var value = (Boolean) data.get("any");
			return field.toQuery(value ? AnyMatcher.create() : NullMatcher.create());
		}

		return field.toQuery(createMatcher(data));
	}

	protected abstract Matcher<?> createMatcher(Map<String, Object> data);
}

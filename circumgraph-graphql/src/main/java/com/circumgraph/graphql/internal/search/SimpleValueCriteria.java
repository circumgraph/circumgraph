package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.values.SimpleValue;

import se.l4.silo.index.AnyMatcher;
import se.l4.silo.index.Matcher;
import se.l4.silo.index.NullMatcher;
import se.l4.silo.index.search.QueryClause;
import se.l4.silo.index.search.query.FieldQuery;

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
		String path
	)
	{
		// TODO: Check if more than one key is present
		if(data.containsKey("any"))
		{
			// TODO: If any = true should always match
			var value = (Boolean) data.get("any");
			if(value == null)
			{
				// TODO: Proper error
			}

			return value
				? FieldQuery.create(path, AnyMatcher.create())
				: FieldQuery.create(path, NullMatcher.create());
		}

		return FieldQuery.create(path, createMatcher(data));
	}

	protected abstract Matcher<?> createMatcher(Map<String, Object> data);
}

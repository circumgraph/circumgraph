package com.circumgraph.storage.search;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.internal.search.QueryPaths;

import se.l4.silo.index.Matcher;
import se.l4.silo.index.search.query.FieldQuery;

public interface QueryPath
{
	interface Branch
		extends QueryPath
	{
		/**
		 * Create a path for querying fields in the given structure.
		 *
		 * @param type
		 * @return
		 */
		Branch polymorphic(StructuredDef type);

		/**
		 * Create a {@link Branch} or {@link Leaf} depending on type.
		 *
		 * @param field
		 * @return
		 */
		QueryPath field(String field);

		/**
		 * Branch down into the given field.
		 *
		 * @param field
		 * @return
		 */
		Branch branch(String field);

		/**
		 * Create a path for querying the given leaf.
		 *
		 * @param field
		 *   name of field to query
		 * @return
		 */
		Leaf leaf(String field);
	}

	interface Leaf
		extends QueryPath
	{
		/**
		 * Create a query at this path.
		 *
		 * @param matcher
		 * @return
		 */
		FieldQuery toQuery(Matcher<?> matcher);
	}

	static Branch root(StructuredDef type)
	{
		return QueryPaths.create(type);
	}
}

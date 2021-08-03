package com.circumgraph.storage.search;

import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.internal.search.QueryPathImpl;

import se.l4.silo.index.Matcher;
import se.l4.silo.index.search.query.FieldQuery;

public interface QueryPath
{
	/**
	 * Create a path for querying fields in the given structure.
	 *
	 * @param type
	 * @return
	 */
	QueryPath polymorphic(StructuredDef type);

	/**
	 * Create a path for the given field.
	 *
	 * @param field
	 * @return
	 */
	QueryPath field(String field);

	/**
	 * Create a path pointing to the name of the type.
	 *
	 * @return
	 */
	QueryPath typename();

	/**
	 * Get if this path represents the root object.
	 *
	 * @return
	 */
	boolean isRoot();

	/**
	 * If this path points to a {@link SimpleValueDef} create a {@link FieldQuery}
	 * that matches the field.
	 *
	 * @param matcher
	 * @return
	 */
	FieldQuery toQuery(Matcher<?> matcher);

	String toIndexName();

	static QueryPath root(StructuredDef type)
	{
		return QueryPathImpl.create(type);
	}
}

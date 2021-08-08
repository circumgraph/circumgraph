package com.circumgraph.graphql.internal.resolvers;

import java.util.Optional;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.internal.StorageContext;
import com.circumgraph.graphql.internal.directives.RelationDirectiveProcessor;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.search.Page;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import graphql.schema.DataFetchingEnvironment;
import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.FieldSort;

/**
 * Resolver for fields that use the {@code relation} directive when a simple list
 * is being returned.
 *
 * Validated and created by {@link RelationDirectiveProcessor}.
 */
public class RelationListFieldResolver
	implements FieldResolver
{
	private final Collection other;
	private final QueryPath path;
	private final Optional<FieldSort> sort;

	public RelationListFieldResolver(
		Collection other,
		QueryPath path,
		Optional<FieldSort> sort
	)
	{
		this.other = other;
		this.path = path;
		this.sort = sort;
	}

	@Override
	public Object resolve(DataFetchingEnvironment env)
	{
		StoredObjectValue source = env.getSource();
		StorageContext ctx = env.getContext();

		var query = Query.create()
			.withPage(Page.infinite())
			.addClause(path.toQuery(EqualsMatcher.create(source.getId())));

		if(sort.isPresent())
		{
			query = query.addSort(sort.get());
		}

		return ctx.getTx()
			.wrap(other.search(query))
			.map(o -> o.getNodes());
	}
}

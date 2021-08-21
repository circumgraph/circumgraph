package com.circumgraph.graphql.internal.resolvers;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.FieldResolverFactory;
import com.circumgraph.graphql.GraphQLCreationEncounter;
import com.circumgraph.graphql.internal.processors.RelationDirectiveProcessor;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.search.Page;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.FieldSort;

/**
 * Resolver for fields that use the {@code relation} directive when a simple list
 * is being returned.
 *
 * Validated and created by {@link RelationDirectiveProcessor}.
 */
public class RelationListFieldResolverFactory
	implements FieldResolverFactory
{
	private final String entity;
	private final QueryPath path;
	private final Optional<FieldSort> sort;

	public RelationListFieldResolverFactory(
		String entity,
		QueryPath path,
		Optional<FieldSort> sort
	)
	{
		this.entity = entity;
		this.path = path;
		this.sort = sort;
	}

	@Override
	public FieldResolver create(GraphQLCreationEncounter encounter)
	{
		var collection = encounter.getStorage().get(entity);
		return env -> {
			StoredObjectValue source = env.getSource();

			var query = Query.create()
				.withPage(env.containsArgument("first")
					? Page.first(env.getArgument("first"))
					: Page.infinite()
				)
				.addClause(path.toQuery(EqualsMatcher.create(source.getId())));

			if(sort.isPresent())
			{
				query = query.addSort(sort.get());
			}

			return collection.search(query)
				.map(o -> o.getNodes());
		};
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(entity, path, sort);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		RelationListFieldResolverFactory other = (RelationListFieldResolverFactory) obj;
		return Objects.equals(entity, other.entity)
			&& Objects.equals(path, other.path)
			&& Objects.equals(sort, other.sort);
	}
}

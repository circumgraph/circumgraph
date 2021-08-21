package com.circumgraph.graphql.internal.resolvers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.FieldResolverFactory;
import com.circumgraph.graphql.GraphQLCreationEncounter;
import com.circumgraph.graphql.internal.search.Criteria;
import com.circumgraph.graphql.internal.search.CursorEncoding;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.search.Page;
import com.circumgraph.storage.search.Query;
import com.circumgraph.storage.search.QueryPath;

import se.l4.silo.index.FieldSort;

public class CollectionSearchResolver
	implements FieldResolverFactory
{
	private final StructuredDef def;
	private final Criteria criteria;

	public CollectionSearchResolver(
		StructuredDef def,
		Criteria criteria
	)
	{
		this.def = def;
		this.criteria = criteria;
	}

	@Override
	public FieldResolver create(GraphQLCreationEncounter encounter)
	{
		var collection = encounter.getStorage().get(def.getName());
		return env -> {
			var query = Query.create();

			if(env.containsArgument("criteria"))
			{
				List<Map<String, Object>> args = env.getArgument("criteria");
				var path = QueryPath.root(def);
				for(var e : args)
				{
					query = query.addClause(criteria.toClause(e, path));
				}
			}

			if(env.containsArgument("sort"))
			{
				List<Map<String, Object>> sort = env.getArgument("sort");
				if(! sort.isEmpty())
				{
					for(var s : sort)
					{
						query = query.addSort(FieldSort.create(
							((QueryPath) s.get("field")).toIndexName(),
							(Boolean) s.get("ascending")
						));
					}
				}
			}

			query = query.withPage(Page.first(
				env.getArgumentOrDefault("first", 10),
				CursorEncoding.decode(env.getArgument("after"))
			));

			// Check if scores are being fetched
			query = query.withScoresNeeded(
				env.getSelectionSet().contains("edges/score")
			);

			return collection.search(query);
		};
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(def);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		CollectionSearchResolver other = (CollectionSearchResolver) obj;
		return Objects.equals(def, other.def);
	}
}

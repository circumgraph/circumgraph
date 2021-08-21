package com.circumgraph.graphql.internal.resolvers;

import java.util.Objects;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.FieldResolverFactory;
import com.circumgraph.graphql.GraphQLCreationEncounter;
import com.circumgraph.graphql.internal.StorageIds;
import com.circumgraph.model.StructuredDef;

public class CollectionDeleteMutation
	implements FieldResolverFactory
{
	private final StructuredDef def;

	public CollectionDeleteMutation(
		StructuredDef def
	)
	{
		this.def = def;
	}

	@Override
	public FieldResolver create(GraphQLCreationEncounter encounter)
	{
		var collection = encounter.getStorage().get(def.getName());
		return env -> {
			var id = StorageIds.decode(env.getArgument("id"));
			return collection.delete(id);
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
		CollectionDeleteMutation other = (CollectionDeleteMutation) obj;
		return Objects.equals(def, other.def);
	}
}

package com.circumgraph.graphql.internal.resolvers;

import java.util.Objects;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.FieldResolverFactory;
import com.circumgraph.graphql.GraphQLCreationEncounter;
import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.internal.StorageIds;
import com.circumgraph.storage.mutation.StructuredMutation;

public class CollectionStoreMutation
	implements FieldResolverFactory
{
	private final MutationInputMapper<?> mutationMapper;

	public CollectionStoreMutation(
		MutationInputMapper<?> mutationMapper
	)
	{
		this.mutationMapper = mutationMapper;
	}

	@Override
	public FieldResolver create(GraphQLCreationEncounter encounter)
	{
		var collection = encounter.getStorage().get(mutationMapper.getModelDef().getName());
		return env -> {
			var id = env.getArgument("id") != null ? StorageIds.decode(env.getArgument("id")) : 0;
			var mutation =  (StructuredMutation) mutationMapper.toMutation(env.getArgument("mutation"));

			if(id > 0)
			{
				return collection.store(id, mutation);
			}
			else
			{
				return collection.store(mutation);
			}
		};
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(mutationMapper);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		CollectionStoreMutation other = (CollectionStoreMutation) obj;
		return Objects.equals(mutationMapper, other.mutationMapper);
	}
}

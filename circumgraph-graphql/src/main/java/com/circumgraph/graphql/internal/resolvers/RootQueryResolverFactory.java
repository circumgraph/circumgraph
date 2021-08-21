package com.circumgraph.graphql.internal.resolvers;

import java.util.Objects;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.FieldResolverFactory;
import com.circumgraph.graphql.GraphQLCreationEncounter;
import com.circumgraph.model.StructuredDef;

public class RootQueryResolverFactory
	implements FieldResolverFactory
{
	private final String name;

	public RootQueryResolverFactory(StructuredDef def)
	{
		this.name = def.getName();
	}

	@Override
	public FieldResolver create(GraphQLCreationEncounter encounter)
	{
		var collection = encounter.getStorage().get(name);
		return env -> collection;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		RootQueryResolverFactory other = (RootQueryResolverFactory) obj;
		return Objects.equals(name, other.name);
	}
}

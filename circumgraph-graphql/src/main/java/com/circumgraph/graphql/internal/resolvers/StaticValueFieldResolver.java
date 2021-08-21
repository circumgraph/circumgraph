package com.circumgraph.graphql.internal.resolvers;

import java.util.Objects;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;

import graphql.schema.DataFetchingEnvironment;

/**
 * {@link FieldResolver} that fetches a certain key in a
 * {@link StructuredValue}.
 */
public class StaticValueFieldResolver
	implements FieldResolver
{
	private final String key;

	public StaticValueFieldResolver(String key)
	{
		this.key = key;
	}

	@Override
	public Value resolve(DataFetchingEnvironment env)
	{
		StructuredValue source = env.getSource();
		return source.getField(key).orElse(null);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(key);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		StaticValueFieldResolver other = (StaticValueFieldResolver) obj;
		return Objects.equals(key, other.key);
	}
}

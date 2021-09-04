package com.circumgraph.graphql.internal.mutation;

import java.util.Objects;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.ScalarValueMutation;

/**
 * Mapper used for scalars.
 */
public class ScalarMutationMapper
	implements MutationInputMapper<Object>
{
	private final ScalarDef def;

	public ScalarMutationMapper(
		ScalarDef def
	)
	{
		this.def = def;
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return def;
	}

	@Override
	public InputTypeDef getGraphQLType()
	{
		return def;
	}

	@Override
	public Mutation toMutation(Object value)
	{
		return ScalarValueMutation.create(def, value);
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
		ScalarMutationMapper other = (ScalarMutationMapper) obj;
		return Objects.equals(def, other.def);
	}
}

package com.circumgraph.graphql.internal.mutation;

import java.util.Objects;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.ScalarValueMutation;

/**
 * Mapper used for scalars.
 */
public class ScalarMutationMapper
	implements MutationInputMapper<Object>
{
	private final ScalarMapper<?> scalar;

	public ScalarMutationMapper(
		ScalarMapper<?> scalar
	)
	{
		this.scalar = scalar;
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return scalar.getModelDef();
	}

	@Override
	public InputTypeDef getGraphQLType()
	{
		return scalar.getModelDef();
	}

	@Override
	public Mutation toMutation(Object value)
	{
		return ScalarValueMutation.create(
			scalar.getModelDef(),
			scalar.fromInput(value)
		);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(scalar);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ScalarMutationMapper other = (ScalarMutationMapper) obj;
		return Objects.equals(scalar, other.scalar);
	}
}

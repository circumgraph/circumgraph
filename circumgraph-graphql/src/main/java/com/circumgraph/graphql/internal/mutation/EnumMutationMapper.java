package com.circumgraph.graphql.internal.mutation;

import java.util.Objects;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.SetEnumValueMutation;

public class EnumMutationMapper
	implements MutationInputMapper<String>
{
	private final EnumDef def;

	public EnumMutationMapper(EnumDef def)
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
	public Mutation toMutation(String value)
	{
		return SetEnumValueMutation.create(value);
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
		EnumMutationMapper other = (EnumMutationMapper) obj;
		return Objects.equals(def, other.def);
	}
}

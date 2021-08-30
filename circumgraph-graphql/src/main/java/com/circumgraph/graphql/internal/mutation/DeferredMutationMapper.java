package com.circumgraph.graphql.internal.mutation;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.internal.SchemaNames;
import com.circumgraph.graphql.internal.processors.MutationProcessor;
import com.circumgraph.model.HasMetadata;
import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeRef;
import com.circumgraph.storage.mutation.Mutation;

public class DeferredMutationMapper<V>
	implements MutationInputMapper<V>
{
	private final HasMetadata def;

	@SuppressWarnings("rawtypes")
	private MutationInputMapper mapper;

	public DeferredMutationMapper(HasMetadata def)
	{
		this.def = def;
	}

	@SuppressWarnings("rawtypes")
	private MutationInputMapper mapper()
	{
		var mapper = this.mapper;
		if(mapper == null)
		{
			mapper = def.getMetadata(MutationProcessor.MAPPER).get();
			this.mapper = mapper;
		}

		return mapper;
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return (OutputTypeDef) def;
	}

	@Override
	public InputTypeDef getGraphQLType()
	{
		return TypeRef.create(SchemaNames.toMutationInputTypeName((OutputTypeDef) def));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Mutation toMutation(V value)
	{
		return mapper().toMutation(value);
	}
}

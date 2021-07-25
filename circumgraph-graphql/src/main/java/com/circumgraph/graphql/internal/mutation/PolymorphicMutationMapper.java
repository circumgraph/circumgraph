package com.circumgraph.graphql.internal.mutation;

import java.util.Map;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.mutation.Mutation;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;

public class PolymorphicMutationMapper
	implements MutationInputMapper<Map<String, Object>>
{
	private final OutputTypeDef modelDef;
	private final GraphQLInputType graphQLType;
	private final ImmutableMap<String, MutationInputMapper<?>> mappers;

	public PolymorphicMutationMapper(
		OutputTypeDef modelDef,
		RichIterable<? extends MutationInputMapper<?>> types
	)
	{
		this.modelDef = modelDef;

		GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
			.name(modelDef.getName() + "MutationInput")
			.description("Mutation for " + modelDef.getName());

		MutableMap<String, MutationInputMapper<?>> fieldToMapper = Maps.mutable.empty();
		for(var mapper : types)
		{
			var fieldName = mapper.getModelDef().getName();

			builder.field(GraphQLInputObjectField.newInputObjectField()
				.name(fieldName)
				.type(mapper.getGraphQLType())
			);

			fieldToMapper.put(fieldName, mapper);
		}

		this.mappers = fieldToMapper.toImmutable();
		this.graphQLType = builder.build();
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return modelDef;
	}

	@Override
	public GraphQLInputType getGraphQLType()
	{
		return graphQLType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Mutation toMutation(Map<String, Object> value)
	{
		// TODO: This should validate that exactly one field has been provided

		for(var entry : value.entrySet())
		{
			return ((MutationInputMapper) mappers.get(entry.getKey()))
				.toMutation(entry.getValue());
		}

		throw new RuntimeException();
	}
}

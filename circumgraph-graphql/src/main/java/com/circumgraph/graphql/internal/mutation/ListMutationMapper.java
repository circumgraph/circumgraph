package com.circumgraph.graphql.internal.mutation;

import java.util.Map;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.internal.InputUnions;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.mutation.Mutation;

import org.eclipse.collections.api.factory.Lists;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;

public class ListMutationMapper<V>
	implements MutationInputMapper<Map<String, Object>>
{
	private final ListDef.Output modelDef;
	private final GraphQLInputObjectType graphQLType;
	private final MutationInputMapper<V> itemMapper;

	public ListMutationMapper(
		ListDef.Output modelDef,
		MutationInputMapper<V> itemMapper
	)
	{
		this.modelDef = modelDef;
		this.itemMapper = itemMapper;

		var itemType = modelDef.getItemType();
		var wasNonNull = false;
		if(itemType instanceof NonNullDef.Output)
		{
			itemType = ((NonNullDef.Output) itemType).getType();
			wasNonNull = true;
		}

		var builder = GraphQLInputObjectType.newInputObject()
			.name(itemType.getName() + "ListMutationInput")
			.description("Mutation of a list of " + itemType.getName())
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("set")
				.description("Values to set")
				.type(GraphQLList.list(
					wasNonNull
						? GraphQLNonNull.nonNull(itemMapper.getGraphQLType())
						: itemMapper.getGraphQLType()
				))
				.build()
			);

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
	public Mutation toMutation(Map<String, Object> value)
	{
		InputUnions.validate(graphQLType, value);

		var set = value.get("set");
		return ListSetMutation.create(
			Lists.immutable.ofAll((Iterable<V>) set)
				.collect(itemMapper::toMutation)
		);
	}
}

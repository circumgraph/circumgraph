package com.circumgraph.graphql.internal.mutation;

import java.util.Map;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.internal.InputUnions;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import se.l4.ylem.ids.LongIdCodec;

public class StoredObjectRefMutationMapper
	implements MutationInputMapper<Map<String, Object>>
{
	private final LongIdCodec<String> idCodec;

	private final StructuredDef modelDef;
	private final GraphQLInputObjectType graphQLType;

	public StoredObjectRefMutationMapper(
		LongIdCodec<String> idCodec,
		StructuredDef modelDef
	)
	{
		this.modelDef = modelDef;
		this.idCodec = idCodec;

		this.graphQLType = GraphQLInputObjectType.newInputObject()
			.name(modelDef.getName() + "RefInput")
			.description("Reference to a " + modelDef.getName())
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("id")
				.description("Identifier of object to reference")
				.type(Scalars.GraphQLID)
				.build()
			)
			.build();
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

		var id = value.get("id");
		if(id == null)
		{
			throw new RuntimeException();
		}

		return StoredObjectRefMutation.create(modelDef, idCodec.decode(id.toString()));
	}
}

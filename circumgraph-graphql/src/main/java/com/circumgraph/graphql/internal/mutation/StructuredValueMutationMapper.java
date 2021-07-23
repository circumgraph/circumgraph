package com.circumgraph.graphql.internal.mutation;

import java.util.Map;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.mutation.StructuredMutation;

import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.impl.tuple.Tuples;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;

public class StructuredValueMutationMapper
	implements MutationInputMapper<Map<String, Object>>
{
	private final StructuredDef modelDef;
	private final GraphQLInputType graphQLType;

	private final MapIterable<String, MutationInputMapper<?>> fields;

	public StructuredValueMutationMapper(
		StructuredDef modelDef,
		MapIterable<FieldDef, ? extends MutationInputMapper<?>> fields
	)
	{
		this.modelDef = modelDef;

		GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
			.name(modelDef.getName() + "MutationInput")
			.description("Mutation for " + modelDef.getName());

		for(var keyValue : fields.keyValuesView())
		{
			var field = keyValue.getOne();
			var mapper = keyValue.getTwo();

			builder.field(GraphQLInputObjectField.newInputObjectField()
				.name(field.getName())
				.description(field.getDescription().orElse(null))
				.type(mapper.getGraphQLType()
			));
		}

		this.graphQLType = builder.build();
		this.fields = fields.collect((f, m) -> Tuples.pair(f.getName(), m));
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
		StructuredMutation.Builder builder = StructuredMutation.create(this.modelDef);
		for(var entry : value.entrySet())
		{
			var name = entry.getKey();
			var mapper = fields.get(name);

			var fieldValue = entry.getValue();
			if(fieldValue == null)
			{
				// This is being set to null - clear it
				builder = builder.updateField(name, NullMutation.create());
			}
			else
			{
				builder = builder.updateField(
					name,
					((MutationInputMapper) mapper).toMutation(fieldValue)
				);
			}
		}

		return builder.build();
	}
}

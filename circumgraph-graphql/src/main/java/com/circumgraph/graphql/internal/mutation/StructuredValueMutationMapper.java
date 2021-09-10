package com.circumgraph.graphql.internal.mutation;

import java.util.Map;
import java.util.Objects;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.internal.SchemaNames;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.NullMutation;
import com.circumgraph.storage.mutation.StructuredMutation;

import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.impl.tuple.Tuples;

public class StructuredValueMutationMapper
	implements MutationInputMapper<Map<String, Object>>
{
	private final StructuredDef modelDef;
	private final InputObjectDef graphQLType;

	private final MapIterable<String, MutationInputMapper<?>> fields;

	public StructuredValueMutationMapper(
		StructuredDef modelDef,
		MapIterable<FieldDef, ? extends MutationInputMapper<?>> fields
	)
	{
		this.modelDef = modelDef;

		var builder = InputObjectDef.create(SchemaNames.toMutationInputTypeName(modelDef))
			.withDescription("Mutation for " + modelDef.getName());

		for(var keyValue : fields.keyValuesView())
		{
			var field = keyValue.getOne();
			var mapper = keyValue.getTwo();

			builder = builder.addField(InputFieldDef.create(SchemaNames.toInputFieldName(field))
				.withDescription(field.getDescription().orElse(null))
				.withType(mapper.getGraphQLType())
				.build()
			);
		}

		this.graphQLType = builder.build();
		this.fields = fields.collect((f, m) -> Tuples.pair(SchemaNames.toInputFieldName(f), m));
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return modelDef;
	}

	@Override
	public InputObjectDef getGraphQLType()
	{
		return graphQLType;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
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

	@Override
	public int hashCode()
	{
		return Objects.hash(fields, graphQLType, modelDef);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		StructuredValueMutationMapper other =
			(StructuredValueMutationMapper) obj;
		return Objects.equals(fields, other.fields)
			&& Objects.equals(graphQLType, other.graphQLType)
			&& Objects.equals(modelDef, other.modelDef);
	}
}

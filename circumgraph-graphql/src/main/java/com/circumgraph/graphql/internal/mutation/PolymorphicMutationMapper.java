package com.circumgraph.graphql.internal.mutation;

import java.util.Map;
import java.util.Objects;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.internal.InputUnions;
import com.circumgraph.graphql.internal.SchemaNames;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.mutation.Mutation;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

public class PolymorphicMutationMapper
	implements MutationInputMapper<Map<String, Object>>
{
	private final OutputTypeDef modelDef;
	private final InputObjectDef graphQLType;
	private final ImmutableMap<String, MutationInputMapper<?>> mappers;

	public PolymorphicMutationMapper(
		OutputTypeDef modelDef,
		RichIterable<? extends MutationInputMapper<?>> types
	)
	{
		this.modelDef = modelDef;

		var builder = InputObjectDef.create(SchemaNames.toMutationInputTypeName(modelDef))
			.withDescription("Mutation for " + modelDef.getName());

		MutableMap<String, MutationInputMapper<?>> fieldToMapper = Maps.mutable.empty();
		for(var mapper : types)
		{
			var fieldName = SchemaNames.toInputFieldName(mapper.getModelDef());

			builder = builder.addField(InputFieldDef.create(fieldName)
				.withType(mapper.getGraphQLType())
				.build()
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
	public InputObjectDef getGraphQLType()
	{
		return graphQLType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Mutation toMutation(Map<String, Object> value)
	{
		InputUnions.validate(graphQLType, value);

		for(var entry : value.entrySet())
		{
			return ((MutationInputMapper) mappers.get(entry.getKey()))
				.toMutation(entry.getValue());
		}

		throw new RuntimeException();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(graphQLType, mappers, modelDef);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		PolymorphicMutationMapper other = (PolymorphicMutationMapper) obj;
		return Objects.equals(graphQLType, other.graphQLType)
			&& Objects.equals(mappers, other.mappers)
			&& Objects.equals(modelDef, other.modelDef);
	}
}

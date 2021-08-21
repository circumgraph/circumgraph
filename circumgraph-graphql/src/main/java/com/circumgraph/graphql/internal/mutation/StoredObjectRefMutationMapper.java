package com.circumgraph.graphql.internal.mutation;

import java.util.Map;
import java.util.Objects;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.internal.InputUnions;
import com.circumgraph.graphql.internal.SchemaNames;
import com.circumgraph.graphql.internal.StorageIds;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.StoredObjectRefMutation;

public class StoredObjectRefMutationMapper
	implements MutationInputMapper<Map<String, Object>>
{
	private final StructuredDef modelDef;
	private final InputObjectDef graphQLType;

	public StoredObjectRefMutationMapper(
		StructuredDef modelDef
	)
	{
		this.modelDef = modelDef;

		this.graphQLType = InputObjectDef.create(SchemaNames.toRefInputTypeName(modelDef))
			.withDescription("Reference to a " + modelDef.getName())
			.addField(InputFieldDef.create("id")
				.withType(NonNullDef.input(ScalarDef.ID))
				.withDescription("Identifier of object to reference")
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
	public InputObjectDef getGraphQLType()
	{
		return graphQLType;
	}

	@Override
	public Mutation toMutation(Map<String, Object> value)
	{
		InputUnions.validate(graphQLType, value);

		var id = (String) value.get("id");
		return StoredObjectRefMutation.create(modelDef, StorageIds.decode(id));
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(graphQLType, modelDef);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		StoredObjectRefMutationMapper other =
			(StoredObjectRefMutationMapper) obj;
		return Objects.equals(graphQLType, other.graphQLType)
			&& Objects.equals(modelDef, other.modelDef);
	}
}

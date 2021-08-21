package com.circumgraph.graphql.internal.mutation;

import java.util.Map;
import java.util.Objects;

import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.internal.InputUnions;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.mutation.Mutation;

import org.eclipse.collections.api.factory.Lists;

public class ListMutationMapper<V>
	implements MutationInputMapper<Map<String, Object>>
{
	private final ListDef.Output modelDef;
	private final InputObjectDef graphQLType;
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
		if(itemType instanceof NonNullDef.Output n)
		{
			itemType = n.getType();
			wasNonNull = true;
		}

		var builder = InputObjectDef.create(itemType.getName() + "ListMutationInput")
			.withDescription("Mutation of a list of " + itemType.getName())
			.addField(InputFieldDef.create("set")
				.withDescription("Values to set")
				.withType(NonNullDef.input(
					ListDef.input(
						wasNonNull
							? NonNullDef.input(itemMapper.getGraphQLType())
							: itemMapper.getGraphQLType()
					)
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
	public InputObjectDef getGraphQLType()
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

	@Override
	public int hashCode()
	{
		return Objects.hash(graphQLType, itemMapper, modelDef);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ListMutationMapper other = (ListMutationMapper) obj;
		return Objects.equals(graphQLType, other.graphQLType)
			&& Objects.equals(itemMapper, other.itemMapper)
			&& Objects.equals(modelDef, other.modelDef);
	}
}

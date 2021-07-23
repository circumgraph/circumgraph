package com.circumgraph.graphql.internal.output;

import com.circumgraph.graphql.OutputMapper;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.values.StructuredValue;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

/**
 * Mapper for {@link StructuredDef} and {@link StructuredValue}. Simply
 * references the type and returns an unmodified value as it is a
 * {@link graphql.schema.GraphQLObjectType} or {@link graphql.schema.GraphQLInterfaceType}
 * will be generated for it from the current {@link com.circumgraph.model.Model}.
 */
public class StructuredValueOutputMapper
	implements OutputMapper<StructuredValue, StructuredValue>
{
	private final GraphQLTypeReference type;

	public StructuredValueOutputMapper(
		StructuredDef type
	)
	{
		this.type = GraphQLTypeReference.typeRef(type.getName());
	}

	@Override
	public GraphQLOutputType getGraphQLType()
	{
		return type;
	}

	@Override
	public StructuredValue toOutput(StructuredValue in)
	{
		return in;
	}
}

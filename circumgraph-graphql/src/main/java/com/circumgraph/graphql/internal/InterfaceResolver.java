package com.circumgraph.graphql.internal;

import com.circumgraph.values.StructuredValue;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;

/**
 * Resolver that resolves interfaces simply looking up their type via the
 * name.
 */
public class InterfaceResolver
	implements TypeResolver
{
	@Override
	public GraphQLObjectType getType(TypeResolutionEnvironment env)
	{
		StructuredValue value = env.getObject();
		return (GraphQLObjectType) env.getSchema()
			.getType(value.getDefinition().getName());
	}
}

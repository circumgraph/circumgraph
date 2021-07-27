package com.circumgraph.graphql.internal.search;

import java.util.Map;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.Matcher;

/**
 * Criteria for boolean value.
 */
public class BooleanCriteria
	extends SimpleValueCriteria
{
	private static final String DESCRIPTION = """
		Criteria used to match a Boolean.

		All fields within this criteria are nullable, but exactly one field
		needs to be present for the criteria to be valid.

		Use `equals` to specify a Boolean to match against. `any` can be set to
		`true` to match if any Boolean is present, if set to `false` it will only
		match if no Boolean is present.
	""";

	private final GraphQLInputObjectType graphQLType;

	public BooleanCriteria()
	{
		this.graphQLType = GraphQLInputObjectType.newInputObject()
			.name("IDCriteriaInput")
			.description(DESCRIPTION)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("any")
				.description("Match if any value is present")
				.type(Scalars.GraphQLBoolean)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("equals")
				.description("Boolean should equal the given value")
				.type(Scalars.GraphQLBoolean)
			)
			.build();
	}

	@Override
	public GraphQLInputObjectType getGraphQLType()
	{
		return graphQLType;
	}

	@Override
	public Matcher<?> createMatcher(Map<String, Object> data)
	{
		if(data.containsKey("equals"))
		{
			return EqualsMatcher.create(data.get("equals"));
		}

		throw new RuntimeException();
	}
}

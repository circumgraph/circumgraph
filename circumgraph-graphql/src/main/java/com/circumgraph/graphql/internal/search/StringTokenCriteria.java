package com.circumgraph.graphql.internal.search;

import java.util.Map;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.Matcher;

public class StringTokenCriteria
	extends SimpleValueCriteria
{
	private static final String DESCRIPTION = """
		Criteria used to match a String indexed as a token.

		All fields within this criteria are nullable, but exactly one field
		needs to be present for the criteria to be valid.

		Use `equals` to specify an exact String to match against. `any` can be
		set to true` to match if any String is present, if set to `false` it
		will only match if no String is present.
	""";

	private final GraphQLInputObjectType graphQLType;

	public StringTokenCriteria()
	{
		this.graphQLType = GraphQLInputObjectType.newInputObject()
			.name("StringTokenCriteriaInput")
			.description(DESCRIPTION)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("any")
				.description("Match if any value is present")
				.type(Scalars.GraphQLBoolean)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("equals")
				.description("String should equal the given value")
				.type(Scalars.GraphQLString)
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

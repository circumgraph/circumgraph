package com.circumgraph.graphql.internal.search;

import java.util.Map;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import se.l4.silo.index.Matcher;
import se.l4.silo.index.search.query.UserQueryMatcher;

public class StringTypeAheadCriteria
	extends SimpleValueCriteria
{
	private static final String DESCRIPTION = """
		Criteria used to match a String indexed as full text with type-ahead
		support.

		All fields within this criteria are nullable, but exactly one field
		needs to be present for the criteria to be valid.
	""";

	private final GraphQLInputObjectType graphQLType;

	public StringTypeAheadCriteria()
	{
		this.graphQLType = GraphQLInputObjectType.newInputObject()
			.name("StringTypeAheadCriteriaInput")
			.description(DESCRIPTION)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("any")
				.description("Match if any value is present")
				.type(Scalars.GraphQLBoolean)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("match")
				.description("Match against full text of field")
				.type(Scalars.GraphQLString)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("matchTypeAhead")
				.description("Match against type ahead")
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
		if(data.get("match") != null)
		{
			return UserQueryMatcher.standard(
				(String) data.get("match")
			);
		}
		else if(data.get("matchTypeAhead") != null)
		{
			return UserQueryMatcher.standard(
				(String) data.get("matchTypeAhead")
			);
		}

		throw new RuntimeException();
	}
}

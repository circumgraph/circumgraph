package com.circumgraph.graphql.internal.search;

import java.util.Map;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNonNull;
import se.l4.silo.index.Matcher;
import se.l4.silo.index.search.query.UserQuery;

public class StringFullTextCriteria
	extends SimpleValueCriteria
{
	private static final String DESCRIPTION = """
		Criteria used to match a String indexed as full text.

		All fields within this criteria are nullable, but exactly one field
		needs to be present for the criteria to be valid.
	""";

	private final GraphQLInputObjectType graphQLType;

	public StringFullTextCriteria()
	{
		this.graphQLType = GraphQLInputObjectType.newInputObject()
			.name("StringFullTextCriteriaInput")
			.description(DESCRIPTION)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("any")
				.description("Match if any value is present")
				.type(Scalars.GraphQLBoolean)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("match")
				.description("Match against full text of field")
				.type(GraphQLInputObjectType.newInputObject()
					.name("StringMatchCriteriaInput")
					.field(GraphQLInputObjectField.newInputObjectField()
						.name("query")
						.description("The query to match against")
						.type(GraphQLNonNull.nonNull(Scalars.GraphQLString))
					)
					.field(GraphQLInputObjectField.newInputObjectField()
						.name("typeAhead")
						.description("If this is a type-ahead query")
						.type(Scalars.GraphQLBoolean)
						.defaultValue(false)
					)
				)
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
			var match = (Map<String, Object>) data.get("match");
			return UserQuery.matcher(
				(String) match.get("query"),
				match.get("typeAhead") == Boolean.TRUE
					? UserQuery.Context.TYPE_AHEAD
					: UserQuery.Context.STANDARD
			);
		}

		throw new RuntimeException();
	}
}

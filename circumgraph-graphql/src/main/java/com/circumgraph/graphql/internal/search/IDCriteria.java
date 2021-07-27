package com.circumgraph.graphql.internal.search;

import java.util.Map;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.Matcher;
import se.l4.ylem.ids.LongIdCodec;

/**
 * Criteria for ID.
 */
public class IDCriteria
	extends SimpleValueCriteria
{
	private static final String DESCRIPTION = """
		Criteria used to match an ID.

		All fields within this criteria are nullable, but exactly one field
		needs to be present for the criteria to be valid.

		Use `equals` to specify an ID to match against. `any` can be set to
		`true` to match if any ID is present, if set to `false` it will only
		match if no ID is present.
	""";

	private final LongIdCodec<String> idCodec;

	private final GraphQLInputObjectType graphQLType;

	public IDCriteria(LongIdCodec<String> idCodec)
	{
		this.idCodec = idCodec;

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
				.description("ID should equal the given value")
				.type(Scalars.GraphQLID)
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
			var value = (String) data.get("equals");
			if(value == null)
			{
				return EqualsMatcher.create(null);
			}

			try
			{
				return EqualsMatcher.create(idCodec.decode(value));
			}
			catch(RuntimeException e)
			{
				// TODO: Matcher for never?
				return EqualsMatcher.create(0);
			}
		}

		throw new RuntimeException();
	}
}

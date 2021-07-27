package com.circumgraph.graphql.internal.search;

import java.util.Map;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import se.l4.silo.index.AnyMatcher;
import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.Matcher;
import se.l4.silo.index.RangeMatcher;

/**
 * Criteria for matching a {@code Int} type. Allows exact and range matching.
 */
public class IntCriteria
	extends SimpleValueCriteria
{
	private static final String DESCRIPTION = """
		Criteria used to match an Int.

		All fields within this criteria are nullable, but exactly one field
		needs to be present for the criteria to be valid.

		* `any` can be set to `true` to match if any Int is present, or `false`
		   to only match if no Int is present.
		* `equals` can be used to match a value exactly.
		* `range` can be used to match if an Int is within the given range.
	""";

	private static final String RANGE_DESCRIPTION = """
		Criteria used to match if an Int is within a certain range. This
		input object should have one or both of `min` and `max` specified. By
		default both `min` and `max` are exclusive, but this can be controlled
		by setting `minInclusive` and `maxInclusive` to `false`.
	""";

	private final GraphQLInputObjectType graphQLType;

	public IntCriteria()
	{
		this.graphQLType = GraphQLInputObjectType.newInputObject()
			.name("IntCriteriaInput")
			.description(DESCRIPTION)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("any")
				.description("Match if any value is present")
				.type(Scalars.GraphQLBoolean)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("equals")
				.description("Int should equal the given value")
				.type(Scalars.GraphQLInt)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("range")
				.description("Int should be within the given range")
				.type(createRange())
			)
			.build();
	}


	private static GraphQLInputObjectType createRange()
	{
		return GraphQLInputObjectType.newInputObject()
			.name("IntRangeCriteriaInput")
			.description(RANGE_DESCRIPTION)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("min")
				.description("Minimum value that should match, inclusive by default")
				.type(Scalars.GraphQLInt)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("minInclusive")
				.description("If the minimum value should inclusive or exclusive")
				.type(Scalars.GraphQLBoolean)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("max")
				.description("Maximum value that should match, inclusive by default")
				.type(Scalars.GraphQLInt)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("maxInclusive")
				.description("If the maximum value should be inclusive or exclusive")
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
	protected Matcher<?> createMatcher(Map<String, Object> data)
	{
		if(data.containsKey("equals"))
		{
			var equals = data.get("equals");
			return EqualsMatcher.create(equals);
		}
		else if(data.containsKey("range"))
		{
			var range = (Map<String, Object>) data.get("range");
			if(range == null) return null;

			var min = range.get("min");
			var max = range.get("max");

			if(min == null && max == null)
			{
				// Special case, no range specified, assume all
				return AnyMatcher.create();
			}

			var minInclusive = (Boolean) range.get("minInclusive");
			var maxInclusive = (Boolean) range.get("maxInclusive");

			return RangeMatcher.between(
				min,
				minInclusive == null ? true : minInclusive,
				max,
				maxInclusive == null ? true : maxInclusive
			);
		}

		// Returning null lets FieldCriteria raise an error
		return null;
	}
}

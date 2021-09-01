package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;

import se.l4.silo.index.AnyMatcher;
import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.Matcher;
import se.l4.silo.index.RangeMatcher;

public class LocalTimeCriteria
	extends SimpleValueCriteria
{
	private static final String DESCRIPTION = """
		Criteria used to match a LocalTime.

		All fields within this criteria are nullable, but exactly one field
		needs to be present for the criteria to be valid.

		* `any` can be set to `true` to match if any LocalTime is present, or
		  `false` to only match if no LocalTime is present.
		* `equals` can be used to match a value exactly.
		* `range` can be used to match if a LocalTime is within the given range.
	""";

	private static final String RANGE_DESCRIPTION = """
		Criteria used to match if a LocalTime is within a certain range. This
		input object should have one or both of `min` and `max` specified. By
		default both `min` and `max` are exclusive, but this can be controlled
		by setting `minInclusive` and `maxInclusive` to `false`.
	""";

	private final InputObjectDef graphQLType;

	public LocalTimeCriteria()
	{
		this.graphQLType = InputObjectDef.create("LocalTimeCriteriaInput")
			.withDescription(DESCRIPTION)
			.addField(InputFieldDef.create("any")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("Match if any value is present")
				.build()
			)
			.addField(InputFieldDef.create("equals")
				.withType(ScalarDef.LOCAL_TIME)
				.withDescription("LocalTime should equal the given value")
				.build()
			)
			.addField(InputFieldDef.create("range")
				.withType(createRange())
				.withDescription("LocalTime should be within the given range")
				.build()
			)
			.build();
	}


	private static InputObjectDef createRange()
	{
		return InputObjectDef.create("LocalTimeRangeCriteriaInput")
			.withDescription(RANGE_DESCRIPTION)
			.addField(InputFieldDef.create("min")
				.withType(ScalarDef.LOCAL_TIME)
				.withDescription("Minimum value that should match, inclusive by default")
				.build()
			)
			.addField(InputFieldDef.create("minInclusive")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("If the minimum value should inclusive or exclusive")
				.build()
			)
			.addField(InputFieldDef.create("max")
				.withType(ScalarDef.LOCAL_TIME)
				.withDescription("Maximum value that should match, inclusive by default")
				.build()
			)
			.addField(InputFieldDef.create("maxInclusive")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("If the maximum value should be inclusive or exclusive")
				.build()
			)
			.build();
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return ScalarDef.LOCAL_TIME;
	}

	@Override
	public InputObjectDef getGraphQLType()
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

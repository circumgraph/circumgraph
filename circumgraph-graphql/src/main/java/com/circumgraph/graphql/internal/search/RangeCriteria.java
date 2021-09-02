package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;

import se.l4.silo.index.AnyMatcher;
import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.Matcher;
import se.l4.silo.index.RangeMatcher;

public abstract class RangeCriteria
	extends SimpleValueCriteria
{
	private static final String DESCRIPTION = """
		Criteria used to match a {}.

		All fields within this criteria are nullable, but exactly one field
		needs to be present for the criteria to be valid.

		* `any` can be set to `true` to match if any {} is present, or
		  `false` to only match if no {} is present.
		* `equals` can be used to match a value exactly.
		* `range` can be used to match if a {} is within the given range.
	""";

	private static final String RANGE_DESCRIPTION = """
		Criteria used to match if a {} is within a certain range. This
		input object should have one or both of `min` and `max` specified. By
		default both `min` and `max` are exclusive, but this can be controlled
		by setting `minInclusive` and `maxInclusive` to `false`.
	""";

	private final SimpleValueDef def;
	private final InputObjectDef graphQLType;

	public RangeCriteria(
		SimpleValueDef def
	)
	{
		this.def = def;
		var type = def.getName();
		this.graphQLType = InputObjectDef.create(type + "CriteriaInput")
			.withDescription(DESCRIPTION.replace("{}", type))
			.addField(InputFieldDef.create("any")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("Match if any value is present")
				.build()
			)
			.addField(InputFieldDef.create("equals")
				.withType(def)
				.withDescription(type + " should equal the given value")
				.build()
			)
			.addField(InputFieldDef.create("range")
				.withType(createRange(def))
				.withDescription(type + " should be within the given range")
				.build()
			)
			.build();
	}


	private static InputObjectDef createRange(SimpleValueDef def)
	{
		var type = def.getName();
		return InputObjectDef.create(type + "RangeCriteriaInput")
			.withDescription(RANGE_DESCRIPTION.replace("{}", type))
			.addField(InputFieldDef.create("min")
				.withType(def)
				.withDescription("Minimum value that should match, inclusive by default")
				.build()
			)
			.addField(InputFieldDef.create("minInclusive")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("If the minimum value should inclusive or exclusive")
				.build()
			)
			.addField(InputFieldDef.create("max")
				.withType(def)
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
		return def;
	}

	@Override
	public InputObjectDef getGraphQLType()
	{
		return graphQLType;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Matcher<?> createMatcher(Map<String, Object> data)
	{
		if(data.get("equals") != null)
		{
			var equals = data.get("equals");
			return EqualsMatcher.create(equals);
		}
		else if(data.get("range") != null)
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

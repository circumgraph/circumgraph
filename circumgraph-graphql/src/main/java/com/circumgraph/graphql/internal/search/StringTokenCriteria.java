package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;

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

	private final InputObjectDef graphQLType;

	public StringTokenCriteria()
	{
		this.graphQLType = InputObjectDef.create("StringTokenCriteriaInput")
			.withDescription(DESCRIPTION)
			.addField(InputFieldDef.create("any")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("Match if any value is present")
				.build()
			)
			.addField(InputFieldDef.create("equals")
				.withType(ScalarDef.STRING)
				.withDescription("String should equal the given value")
				.build()
			)
			.build();
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return ScalarDef.STRING;
	}

	@Override
	public InputObjectDef getGraphQLType()
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

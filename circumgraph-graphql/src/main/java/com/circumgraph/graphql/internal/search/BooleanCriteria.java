package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.ScalarDef;

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

	private final InputObjectDef graphQLType;

	public BooleanCriteria()
	{
		this.graphQLType = InputObjectDef.create("IDCriteriaInput")
			.withDescription(DESCRIPTION)
			.addField(InputFieldDef.create("any")
				.withType(ScalarDef.STRING)
				.withDescription("Match if any value is present")
				.build()
			)
			.addField(InputFieldDef.create("equals")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("Boolean should equal the given value")
				.build()
			)
			.build();
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

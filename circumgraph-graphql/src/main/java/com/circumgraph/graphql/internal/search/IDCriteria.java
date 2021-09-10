package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;

import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.Matcher;

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

	private final InputObjectDef graphQLType;

	public IDCriteria()
	{
		this.graphQLType = InputObjectDef.create("IDCriteriaInput")
			.withDescription(DESCRIPTION)
			.addField(InputFieldDef.create("any")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("Match if any value is present")
				.build()
			)
			.addField(InputFieldDef.create("equals")
				.withType(ScalarDef.ID)
				.withDescription("ID should equal the given value")
				.build()
			)
			.build();
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return ScalarDef.ID;
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
			var value = (String) data.get("equals");
			if(value == null)
			{
				return EqualsMatcher.create(null);
			}

			try
			{
				return EqualsMatcher.create(value);
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

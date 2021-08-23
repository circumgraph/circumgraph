package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;

import se.l4.silo.index.EqualsMatcher;
import se.l4.silo.index.Matcher;

public class EnumCriteria
	extends SimpleValueCriteria
{
	private static final String DESCRIPTION = """
		Criteria used to match an enum value.

		All fields within this criteria are nullable, but exactly one field
		needs to be present for the criteria to be valid.

		Use `equals` to specify an exact value to match against. `any` can be
		set to true` to match if any value is present, if set to `false` it
		will only match if no value is present.
	""";

	private final EnumDef modelDef;
	private final InputObjectDef graphQLType;

	public EnumCriteria(EnumDef def)
	{
		this.modelDef = def;

		this.graphQLType = InputObjectDef.create(def.getName() + "CriteriaInput")
			.withDescription(DESCRIPTION)
			.addField(InputFieldDef.create("any")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("Match if any value is present")
				.build()
			)
			.addField(InputFieldDef.create("equals")
				.withType(ScalarDef.STRING)
				.withDescription("Enum should equal the given value")
				.build()
			)
			.build();
	}

	@Override
	public OutputTypeDef getModelDef()
	{
		return modelDef;
	}

	@Override
	public InputObjectDef getGraphQLType()
	{
		return graphQLType;
	}

	@Override
	public Matcher<?> createMatcher(Map<String, Object> data)
	{
		if(data.get("equals") != null)
		{
			return EqualsMatcher.create(data.get("equals"));
		}

		throw new RuntimeException();
	}
}

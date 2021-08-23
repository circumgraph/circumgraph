package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;

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

	private final InputObjectDef graphQLType;

	public StringFullTextCriteria()
	{
		this.graphQLType = InputObjectDef.create("StringFullTextCriteriaInput")
			.withDescription(DESCRIPTION)
			.addField(InputFieldDef.create("any")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("Match if any value is present")
				.build()
			)
			.addField(InputFieldDef.create("match")
				.withDescription("Match against full text of field")
				.withType(InputObjectDef.create("StringMatchCriteriaInput")
					.addField(InputFieldDef.create("query")
						.withType(NonNullDef.input(ScalarDef.STRING))
						.withDescription("The query to match against")
						.build()
					)
					.addField(InputFieldDef.create("typeAhead")
						.withType(ScalarDef.BOOLEAN)
						.withDefaultValue(false)
						.withDescription("If this is a type-ahead query")
						.build()
					)
					.build()
				)
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

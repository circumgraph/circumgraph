package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.graphql.internal.InputUnions;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.search.QueryPath;

import org.eclipse.collections.api.map.ImmutableMap;

import se.l4.silo.index.AnyMatcher;
import se.l4.silo.index.NullMatcher;
import se.l4.silo.index.search.QueryClause;

/**
 * Criteria for a {@link UnionDef}.
 *
 * <pre>
 * type UnionCriteria {
 *   any: boolean
 *
 *   specificEntityName: SpecificEntityNameCriteria
 * }
 * </pre>
 */
public class UnionCriteria
	implements Criteria
{
	private static String TYPE_DESCRIPTION = """
		All fields in this type are nullable, but exactly one field must be
		specified for the criteria to be valid. Multiple or no fields will
		raise an error.
	""";

	private final UnionDef def;
	private final InputObjectDef graphQLType;
	private final ImmutableMap<String, Criteria> subTypes;

	public UnionCriteria(
		UnionDef def,
		ImmutableMap<String, Criteria> subTypes
	)
	{
		this.def = def;
		this.subTypes = subTypes;

		var name = def.getName() + "CriteriaInput";

		var builder = InputObjectDef.create(name)
			.withDescription("Query criteria for " + def.getName() + ".\n\n" + TYPE_DESCRIPTION)
			.addField(InputFieldDef.create("any")
				.withType(ScalarDef.BOOLEAN)
				.withDescription("Match any value")
				.build()
			);

		for(var subType : subTypes.keyValuesView())
		{
			builder = builder.addField(InputFieldDef.create(subType.getOne())
				.withType(subType.getTwo().getGraphQLType())
				.build()
			);
		}

		this.graphQLType = builder.build();
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
	public QueryClause toClause(
		Map<String, Object> data,
		QueryPath path
	)
	{
		InputUnions.validate(graphQLType, data);

		String subKey = data.keySet().iterator().next();
		switch(subKey)
		{
			case "any":
				return path.toQuery(data.get("any") == Boolean.TRUE
					? AnyMatcher.create()
					: NullMatcher.create()
				);
			default:
				var subType = subTypes.get(subKey);
				return subType.toClause(
					(Map<String, Object>) data.get(subKey),
					path.polymorphic((StructuredDef) subType.getModelDef())
				);
		}
	}
}

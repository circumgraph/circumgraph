package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.model.StructuredDef;

import org.eclipse.collections.api.map.MapIterable;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import se.l4.silo.index.search.QueryClause;

public class FieldCriteria
	implements Criteria
{
	private static String TYPE_DESCRIPTION = """
		All fields in this type are nullable, but exactly one field must be
		specified for the criteria to be valid. Multiple or no fields will
		raise an error.
	""".trim();

	private final GraphQLInputObjectType graphQLType;
	private final MapIterable<String, Criteria> fields;

	public FieldCriteria(
		StructuredDef def,
		MapIterable<String, Criteria> fields
	)
	{
		this.fields = fields;
		var name = def.getName() + "FieldCriteriaInput";

		var builder = GraphQLInputObjectType.newInputObject()
			.name(name)
			.description("Field criteria for " + def.getName() + ".\n" + TYPE_DESCRIPTION);

		for(var pair : fields.keyValuesView())
		{
			var fieldName = pair.getOne();
			var fieldCriteria = pair.getTwo();

			builder.field(GraphQLInputObjectField.newInputObjectField()
				.name(fieldName)
				.description("Match a criteria against the " + fieldName + " field")
				.type(fieldCriteria.getGraphQLType())
			);
		}

		this.graphQLType = builder.build();
	}

	@Override
	public GraphQLInputObjectType getGraphQLType()
	{
		return graphQLType;
	}

	@Override
	public QueryClause toClause(Map<String, Object> data, String path)
	{
		// TODO: Validate only single field used
		for(var entry : data.entrySet())
		{
			if(entry != null)
			{
				return fields.get(entry.getKey())
					.toClause(
						(Map<String, Object>) entry.getValue(),
						path == null ? entry.getKey() : (path + '.' + entry.getKey())
					);
			}
		}

		return null;
	}
}

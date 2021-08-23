package com.circumgraph.graphql.internal.search;

import java.util.Map;

import com.circumgraph.graphql.internal.InputUnions;
import com.circumgraph.model.InputFieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.search.QueryPath;

import org.eclipse.collections.api.map.MapIterable;

import se.l4.silo.index.search.QueryClause;

/**
 * Criteria that combines {@link Criteria} instances for all the fields in
 * a {@link StructuredDef}.
 */
public class FieldCriteria
	implements Criteria
{
	private static String TYPE_DESCRIPTION = """
		All fields in this type are nullable, but exactly one field must be
		specified for the criteria to be valid. Multiple or no fields will
		raise an error.
	""".trim();

	private final StructuredDef def;
	private final InputObjectDef graphQLType;
	private final MapIterable<String, Criteria> fields;

	public FieldCriteria(
		StructuredDef def,
		MapIterable<String, Criteria> fields
	)
	{
		this.def = def;
		this.fields = fields;
		var name = def.getName() + "FieldCriteriaInput";

		var builder = InputObjectDef.create(name)
			.withDescription("Field criteria for " + def.getName() + ".\n" + TYPE_DESCRIPTION);

		for(var pair : fields.keyValuesView())
		{
			var fieldName = pair.getOne();
			var fieldCriteria = pair.getTwo();

			builder = builder.addField(InputFieldDef.create(fieldName)
				.withType(fieldCriteria.getGraphQLType())
				.withDescription("Match a criteria against the " + fieldName + " field")
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
	public QueryClause toClause(Map<String, Object> data, QueryPath path)
	{
		InputUnions.validate(graphQLType, data);

		for(var entry : data.entrySet())
		{
			if(entry != null)
			{
				return fields.get(entry.getKey())
					.toClause(
						(Map<String, Object>) entry.getValue(),
						path.field(entry.getKey())
					);
			}
		}

		return null;
	}
}

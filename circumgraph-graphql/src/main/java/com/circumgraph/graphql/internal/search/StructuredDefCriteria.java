package com.circumgraph.graphql.internal.search;

import java.util.List;
import java.util.Map;

import com.circumgraph.graphql.internal.InputUnions;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.search.QueryPath;

import org.eclipse.collections.api.map.MapIterable;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLTypeReference;
import se.l4.silo.index.search.QueryClause;
import se.l4.silo.index.search.query.AndQuery;
import se.l4.silo.index.search.query.NegateQuery;
import se.l4.silo.index.search.query.OrQuery;

/**
 * Criteria for a specific entity.
 *
 * <pre>
 * type EntityNameCriteria {
 *   any: boolean
 *   field: EntityNameFieldMatcher
 *   and: [EntityNameCriteria!]
 *   or: [EntityNameCriteria!]
 *   not: EntityNameCriteria
 * }
 * </pre>
 *
 * For interfaces additional fields are generated for concrete implementations
 * that link to the specific implementations:
 *
 * <pre>
 * type EntityNameCriteria {
 *   ...
 *
 *   SpecificEntityName: SpecificEntityNameCriteria
 * }
 * </pre>
 */
public class StructuredDefCriteria
	implements Criteria
{
	private static String TYPE_DESCRIPTION = """
		Criteria is used to limit a search, it supports matching on fields,
		branching out to several other criteria using AND/OR or negating a
		criteria.

		All fields in this type are nullable, but exactly one field must be
		specified for the criteria to be valid. Multiple or no fields will
		raise an error.
	""";

	private final GraphQLInputObjectType graphQLType;

	private final FieldCriteria fieldCriteria;

	public StructuredDefCriteria(
		StructuredDef def,
		FieldCriteria fieldCriteria,
		MapIterable<String, StructuredDefCriteria> subTypes
	)
	{
		this.fieldCriteria = fieldCriteria;
		var name = def.getName() + "CriteriaInput";

		var builder = GraphQLInputObjectType.newInputObject()
			.name(name)
			.description("Query criteria for " + def.getName() + ".\n\n" + TYPE_DESCRIPTION)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("any")
				.description("Match any value")
				.type(Scalars.GraphQLBoolean)
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("field")
				.description("Match against a field in this type")
				.type(fieldCriteria.getGraphQLType())
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("and")
				.description("Create a branch where all criteria must match")
				.type(GraphQLList.list(
					GraphQLNonNull.nonNull(
						GraphQLTypeReference.typeRef(name)
					)
				))
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("or")
				.description("Create a branch where one or more criteria must match")
				.type(GraphQLList.list(
					GraphQLNonNull.nonNull(
						GraphQLTypeReference.typeRef(name)
					)
				))
			)
			.field(GraphQLInputObjectField.newInputObjectField()
				.name("not")
				.description("Create a branch where no criteria can match")
				.type(GraphQLTypeReference.typeRef(name))
			);

		// TODO: Generate specific inputs

		this.graphQLType = builder.build();
	}

	@Override
	public GraphQLInputObjectType getGraphQLType()
	{
		return graphQLType;
	}

	@Override
	public QueryClause toClause(
		Map<String, Object> data,
		QueryPath path
	)
	{
		InputUnions.validate(graphQLType, data);

		if(data.containsKey("any"))
		{
			// TODO: Handle always true somehow
			return null;
		}
		else if(data.containsKey("and"))
		{
			var subCriteria = (List<Map<String, Object>>) data.get("and");
			var builder = AndQuery.create();
			for(var o : subCriteria)
			{
				builder = builder.add(toClause(o, path));
			}

			return builder.build();
		}
		else if(data.containsKey("or"))
		{
			var subCriteria = (List<Map<String, Object>>) data.get("or");
			var builder = OrQuery.create();
			for(var o : subCriteria)
			{
				builder = builder.add(toClause(o, path));
			}

			return builder.build();
		}
		else if(data.containsKey("not"))
		{
			var subClause = (Map<String, Object>) data.get("not");
			return NegateQuery.create(toClause(subClause, path));
		}
		else if(data.containsKey("field"))
		{
			var subClause = (Map<String, Object>) data.get("field");
			return fieldCriteria.toClause(subClause, path);
		}

		// TODO: Polymorphism

		throw new RuntimeException();
	}
}

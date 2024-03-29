package com.circumgraph.graphql.internal.processors;

import java.util.Optional;

import com.circumgraph.graphql.GraphQLModel;
import com.circumgraph.graphql.internal.resolvers.RelationListFieldResolverFactory;
import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.ArgumentUse;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.search.QueryPath;

/**
 * Processor for the directive {@code relation} that is used to create a link
 * to another type.
 *
 * Supported arguments:
 *
 * <ul>
 *   <li>{@code field} - the field that references this type in the other type
 *   <li>{@code search} - if a search query should be generated
 * </ul>
 *
 * Schema creating a link that will return a list of {@code Document} when
 * `User.documents` is fetched:
 *
 * <pre>
 * type Document implements Entity {
 *   creator: User! @index
 * }
 *
 * type User implements Entity {
 *   documents: [Document!]! @relation(field: creator)
 * }
 * </pre>
 *
 * Schema creating a link that will return a list of {@code Book} when
 * `Author.books` is fetched:
 *
 * <pre>
 * type Book implements Entity {
 *   authors: [Author!]! @index
 * }
 *
 * type Author implements Entity {
 *   books: [Book!]! @relation(field: authors)
 * }
 * </pre>
 *
 * Schema generating a search query at `Author.books` that will only search
 * for instances of `Book` that link to the author:
 *
 * <pre>
 * type Book implements Entity {
 *   authors: [Author!]! @index
 * }
 *
 * type Author implements Entity {
 *   books: [Book!]! @relation(field: authors, search: true)
 * }
 * </pre>
 */
public class RelationDirectiveProcessor
	implements DirectiveUseProcessor<FieldDef>
{
	private static final ValidationMessageType INVALID_ARGUMENTS = ValidationMessageType.error()
		.withCode("graphql:@relation:invalid-arguments")
		.withMessage("@relation only supports field and search as arguments")
		.build();

	private static final ValidationMessageType INVALID_FIELD_ARGUMENTS = ValidationMessageType.error()
		.withCode("graphql:@relation:invalid-field-arguments")
		.withMessage("Field with @relation can not have arguments")
		.build();

	private static final ValidationMessageType TYPE_NOT_ENTITY = ValidationMessageType.error()
		.withCode("graphql:@relation:type-not-entity")
		.withArgument("type")
		.withMessage("Unsupported type {{type}}, type must be a stored entity")
		.build();

	private static final ValidationMessageType FIELD_UNKNOWN = ValidationMessageType.error()
		.withCode("graphql:@relation:field-unknown")
		.withArgument("field")
		.withArgument("type")
		.withMessage("The field {{field}} can not be found in {{type}}")
		.build();

	private static final ValidationMessageType FIELD_NOT_INDEXED = ValidationMessageType.error()
		.withCode("graphql:@relation:field-not-indexed")
		.withArgument("field")
		.withArgument("type")
		.withMessage("The field {{field}} in {{type}} is not indexed")
		.build();

	private static final ValidationMessageType FIELD_WRONG_TYPE = ValidationMessageType.error()
		.withCode("graphql:@relation:field-wrong-type")
		.withArgument("field")
		.withArgument("type")
		.withArgument("fieldType")
		.withArgument("selfType")
		.withMessage("The field {{field}} in {{type}} must be of type {{selfType}} but is {{fieldType}}")
		.build();

	@Override
	public String getName()
	{
		return "relation";
	}

	@Override
	public Location getLocation()
	{
		return GraphQLModel.LOCATION;
	}

	@Override
	public Class<FieldDef> getContextType()
	{
		return FieldDef.class;
	}

	@Override
	public void process(
		ProcessingEncounter encounter,
		FieldDef location,
		DirectiveUse directive
	)
	{
		if(GraphQLModel.getFieldResolverFactory(location).isPresent())
		{
			// Already a resolver available, skip updating
			return;
		}

		if(! DirectiveUseProcessor.checkOnlyArguments(directive, "field", "search"))
		{
			encounter.report(INVALID_ARGUMENTS.toMessage()
				.withLocation(location)
				.build()
			);
			return;
		}

		// Validate that field is declared in an entity
		if(! location.getDeclaringType().findImplements(StorageSchema.ENTITY_NAME))
		{
			encounter.report(TYPE_NOT_ENTITY.toMessage()
				.withLocation(location)
				.withArgument("type", location.getDeclaringType().getName())
				.build()
			);
			return;
		}

		// Validate and extract the type
		var type = location.getType();
		if(type instanceof NonNullDef.Output n)
		{
			type = n.getType();
		}

		if(type instanceof ListDef.Output l)
		{
			type = l.getItemType();

			if(type instanceof NonNullDef.Output n)
			{
				type = n.getType();
			}
		}

		if(! (type instanceof StructuredDef) || ! ((StructuredDef) type).findImplements(StorageSchema.ENTITY_NAME))
		{
			encounter.report(TYPE_NOT_ENTITY.toMessage()
				.withLocation(location)
				.withArgument("type", type.getName())
				.build()
			);
			return;
		}

		var entity = StorageModel.getEntity(((StructuredDef) type)).get();

		// Extract the field
		var field = directive.getArgument("field").flatMap(ArgumentUse::getValueAsString);
		if(! field.isPresent())
		{
			encounter.report(INVALID_ARGUMENTS.toMessage()
				.withLocation(location)
				.build()
			);
			return;
		}

		var fieldName = field.get();
		var relationField = entity.pickField(fieldName);
		if(relationField.isEmpty())
		{
			encounter.report(FIELD_UNKNOWN.toMessage()
				.withLocation(location)
				.withArgument("field", fieldName)
				.withArgument("type", type.getName())
				.build()
			);
			return;
		}

		// Check that the field is the correct type
		var relationFieldType = relationField.get().getType();
		if(location.getDeclaringType().isAssignableFrom(relationFieldType))
		{
			encounter.report(FIELD_WRONG_TYPE.toMessage()
				.withLocation(location)
				.withArgument("field", fieldName)
				.withArgument("type", type.getName())
				.withArgument("fieldType", relationFieldType.getName())
				.withArgument("selfType", location.getDeclaringType().getName())
				.build()
			);
			return;
		}

		// Check that it is indexed
		if(! StorageModel.isIndexed(relationField.get()))
		{
			encounter.report(FIELD_NOT_INDEXED.toMessage()
				.withLocation(location)
				.withArgument("field", fieldName)
				.withArgument("type", type.getName())
				.build()
			);
			return;
		}

		var path = QueryPath.root(entity).field(fieldName);
		encounter.edit(location, builder -> builder
			.addArgument(ArgumentDef.create("first")
				.withType(ScalarDef.INT)
				.build()
			)
			.withMetadata(
				GraphQLModel.FIELD_RESOLVER_FACTORY,
				new RelationListFieldResolverFactory(entity.getName(), path, Optional.empty())
			)
		);
	}
}

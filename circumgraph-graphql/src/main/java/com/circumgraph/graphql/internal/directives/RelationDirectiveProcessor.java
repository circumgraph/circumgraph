package com.circumgraph.graphql.internal.directives;

import java.util.Optional;
import java.util.function.Consumer;

import com.circumgraph.graphql.GraphQLModel;
import com.circumgraph.graphql.internal.resolvers.RelationListFieldResolver;
import com.circumgraph.model.ArgumentUse;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.validation.ValidationMessage;
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
	public Class<FieldDef> getContextType()
	{
		return FieldDef.class;
	}

	@Override
	public void process(
		FieldDef location,
		DirectiveUse directive,
		Consumer<ValidationMessage> validationCollector
	)
	{
		if(! DirectiveUseProcessor.checkOnlyArguments(directive, "field", "search"))
		{
			validationCollector.accept(INVALID_ARGUMENTS.toMessage()
				.withLocation(location)
				.build()
			);
			return;
		}

		// Validate that field is declared in an entity
		if(! location.getDeclaringType().findImplements(StorageSchema.ENTITY_NAME))
		{
			validationCollector.accept(TYPE_NOT_ENTITY.toMessage()
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
			validationCollector.accept(TYPE_NOT_ENTITY.toMessage()
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
			validationCollector.accept(INVALID_ARGUMENTS.toMessage()
				.withLocation(location)
				.build()
			);
			return;
		}

		var fieldName = field.get();
		var relationField = entity.pickField(fieldName);
		if(relationField.isEmpty())
		{
			validationCollector.accept(FIELD_UNKNOWN.toMessage()
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
			validationCollector.accept(FIELD_WRONG_TYPE.toMessage()
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
		System.out.println(relationField.get());
		if(! StorageModel.isIndexed(relationField.get()))
		{
			validationCollector.accept(FIELD_NOT_INDEXED.toMessage()
				.withLocation(location)
				.withArgument("field", fieldName)
				.withArgument("type", type.getName())
				.build()
			);
			return;
		}

		var path = QueryPath.root(entity).field(fieldName);

		// Set the final resolver
		GraphQLModel.setFieldResolverFactory(location, encounter -> {
			var other = encounter.getStorage().get(entity.getName());
			return new RelationListFieldResolver(other, path, Optional.empty());
		});
	}
}

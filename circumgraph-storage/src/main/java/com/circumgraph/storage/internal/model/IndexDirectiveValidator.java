package com.circumgraph.storage.internal.model;

import java.util.function.Consumer;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.internal.ValueIndexers;

/**
 * Validator for the {@code index} directive.
 */
public class IndexDirectiveValidator
	implements DirectiveValidator<FieldDef>
{
	private static final ValidationMessageType INVALID_ARGUMENTS = ValidationMessageType.error()
		.withCode("storage:@index:invalid-arguments")
		.withMessage("@index only supports an optional type argument")
		.build();

	private static final ValidationMessageType TYPE_UNSUPPORTED = ValidationMessageType.error()
		.withCode("storage:@index:unsupported-type")
		.withArgument("fieldType")
		.withMessage("No indexers are available for {{fieldType}}")
		.build();

	private static final ValidationMessageType MULTIPLE_INDEXERS = ValidationMessageType.error()
		.withCode("storage:@index:multiple-indexers")
		.withArgument("fieldType")
		.withArgument("supported")
		.withMessage("{{fieldType}} supports multiple indexers, specify type on @index to select one of {{supported}}")
		.build();

	private static final ValidationMessageType INVALID_INDEXER = ValidationMessageType.error()
		.withCode("storage:@index:invalid-indexer")
		.withArgument("name")
		.withMessage("The indexer {{indexer}} does not exist")
		.build();

	private static final ValidationMessageType INDEXER_TYPE_UNSUPPORTED = ValidationMessageType.error()
		.withCode("storage:@index:indexer-unsupported-type")
		.withArgument("name")
		.withArgument("fieldType")
		.withMessage("The indexer {{indexer}} does not support {{fieldType}}")
		.build();

	private final ValueIndexers indexing;

	public IndexDirectiveValidator(
		ValueIndexers indexing
	)
	{
		this.indexing = indexing;
	}

	@Override
	public String getName()
	{
		return "index";
	}

	@Override
	public Class<FieldDef> getContextType()
	{
		return FieldDef.class;
	}

	@Override
	public void validate(
		FieldDef location,
		DirectiveUse directive,
		Consumer<ValidationMessage> validationCollector
	)
	{
		OutputTypeDef fieldType = location.getType();
		while(true)
		{
			if(fieldType instanceof NonNullDef.Output)
			{
				fieldType = ((NonNullDef.Output) fieldType).getType();
			}

			if(fieldType instanceof StructuredDef
				&& ((StructuredDef) fieldType).findImplements(StorageSchema.ENTITY_NAME))
			{
				// Link to another collection
				fieldType = ScalarDef.ID;
				break;
			}
			else if(fieldType instanceof SimpleValueDef)
			{
				// Simple edge value
				break;
			}
			else if(fieldType instanceof ListDef.Output)
			{
				fieldType = ((ListDef.Output) fieldType).getItemType();
			}
			else
			{
				error(location, directive, validationCollector);
				return;
			}
		}

		if(directive.getArguments().isEmpty() && ! DirectiveValidator.checkOnlyArguments(directive, "types"))
		{
			validationCollector.accept(INVALID_ARGUMENTS.toMessage()
				.withLocation(directive.getSourceLocation())
				.build()
			);
			return;
		}

		SimpleValueDef def = (SimpleValueDef) fieldType;
		var type = directive.getArgument("type");
		if(type.isEmpty())
		{
			// No type argument, resolve via best
			var indexer = indexing.guessBestIndexer(def);
			if(indexer.isPresent())
			{
				// Indexer found - update the metadata
				StorageModel.setIndexerType(location, indexer.get().getName());
			}
			else if(indexing.hasMultipleIndexers(def))
			{
				// Multiple indexers, report error and give the user the ones that can be used
				validationCollector.accept(MULTIPLE_INDEXERS.toMessage()
					.withLocation(directive.getSourceLocation())
					.withArgument("fieldType", def.getName())
					.withArgument("supported", indexing.getSupportedIndexers(def))
					.build()
				);
			}
			else
			{
				// Generic error that no indexer is available
				error(location, directive, validationCollector);
			}
		}
		else
		{
			var typeName = (String) type.get().getValue();
			var indexer = indexing.getIndexer(typeName);
			if(indexer.isEmpty())
			{
				validationCollector.accept(INVALID_INDEXER.toMessage()
					.withLocation(directive.getSourceLocation())
					.withArgument("indexer", typeName)
					.build()
				);
			}
			else if(indexer.get().getType() != def)
			{
				validationCollector.accept(INDEXER_TYPE_UNSUPPORTED.toMessage()
					.withLocation(directive.getSourceLocation())
					.withArgument("indexer", typeName)
					.withArgument("fieldType", location.getType().getName())
					.build()
				);
			}
			else
			{
				// Indexer found - update the metadata
				StorageModel.setIndexerType(location, indexer.get().getName());
			}
		}
	}

	private void error(
		FieldDef location,
		DirectiveUse directive,
		Consumer<ValidationMessage> validationCollector
	)
	{
		validationCollector.accept(TYPE_UNSUPPORTED.toMessage()
			.withLocation(directive.getSourceLocation())
			.withArgument("fieldType", location.getType().getName())
			.build()
		);
	}
}

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
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.internal.ValueIndexers;

/**
 * Validator for the {@code index} directive.
 */
public class IndexDirectiveValidator
	implements DirectiveValidator<FieldDef>
{
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
				&& ((StructuredDef) location.getType()).findImplements(StorageSchema.ENTITY_NAME))
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
				// No indexer, let's check if none available or many available
				validationCollector.accept(ValidationMessage.error()
					.withLocation(directive.getSourceLocation())
					.withMessage("The type %s has multiple indexers, need to specify type on @index", def.getName())
					.withCode("storage:index-type-multiple")
					.withArgument("type", def.getName())
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
			if(indexer.isEmpty() || indexer.get().getType() != def)
			{
				validationCollector.accept(ValidationMessage.error()
					.withLocation(directive.getSourceLocation())
					.withMessage("The type %s does not support @index with %s", def.getName(), type)
					.withCode("storage:index-type-unsupported")
					.withArgument("type", location.getType().getName())
					.withArgument("indexType", type)
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
		validationCollector.accept(ValidationMessage.error()
			.withLocation(directive.getSourceLocation())
			.withMessage("The type %s does not support @index", location.getType().getName())
			.withCode("storage:index-type-unsupported")
			.withArgument("type", location.getType().getName())
			.build()
		);
	}
}

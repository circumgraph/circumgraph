package com.circumgraph.storage.internal.processors;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.StorageModel;

/**
 * Validator for the {@code sortable} directive.
 */
public class SortableDirectiveProcessor
	implements DirectiveUseProcessor<FieldDef>
{
	private static final ValidationMessageType INVALID_ARGUMENTS = ValidationMessageType.error()
		.withCode("storage:@sortable:invalid-arguments")
		.withMessage("@sortable does not support arguments")
		.build();

	private static final ValidationMessageType NO_INDEXER = ValidationMessageType.error()
		.withCode("storage:@sortable:not-indexed")
		.withMessage("@sortable is only supported for fields that also use @index")
		.build();

	@Override
	public String getName()
	{
		return "sortable";
	}

	@Override
	public Location getLocation()
	{
		return StorageModel.LOCATION;
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
		if(directive.getArguments().isEmpty())
		{
			if(StorageModel.getIndexer(location).isEmpty())
			{
				encounter.report(NO_INDEXER.toMessage()
					.withLocation(directive)
					.build()
				);
			}
			else
			{
				// Mark the field as sortable
				StorageModel.setSortable(location, true);
			}
		}
		else
		{
			encounter.report(INVALID_ARGUMENTS.toMessage()
				.withLocation(directive.getDefinedAt())
				.build()
			);
		}
	}
}

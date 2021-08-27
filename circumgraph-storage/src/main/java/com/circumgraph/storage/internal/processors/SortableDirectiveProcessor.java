package com.circumgraph.storage.internal.processors;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
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

	@Override
	public String getName()
	{
		return "sortable";
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
			// Mark the field as sortable
			StorageModel.setSortable(location, true);
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

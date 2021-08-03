package com.circumgraph.storage.internal.model;

import java.util.function.Consumer;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.validation.ValidationMessage;
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
		FieldDef location,
		DirectiveUse directive,
		Consumer<ValidationMessage> validationCollector
	)
	{
		if(directive.getArguments().isEmpty())
		{
			// Mark the field as sortable
			StorageModel.setSortable(location, true);
		}
		else
		{
			validationCollector.accept(INVALID_ARGUMENTS.toMessage()
				.withLocation(directive.getSourceLocation())
				.build()
			);
		}
	}
}
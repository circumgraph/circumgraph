package com.circumgraph.storage.internal.model;

import java.util.function.Consumer;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;

/**
 * Validator for the {@code readonly} directive which disables mutations for
 * a property.
 */
public class ReadonlyDirectiveProcessor
	implements DirectiveUseProcessor<FieldDef>
{
	private static final ValidationMessageType INVALID_ARGUMENTS = ValidationMessageType.error()
		.withCode("storage:@readonly:invalid-arguments")
		.withMessage("@readonly does not support arguments")
		.build();

	@Override
	public String getName()
	{
		return "readonly";
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
		if(directive.getArguments().isEmpty()) return;

		validationCollector.accept(INVALID_ARGUMENTS.toMessage()
			.withLocation(directive.getSourceLocation())
			.build()
		);
	}
}

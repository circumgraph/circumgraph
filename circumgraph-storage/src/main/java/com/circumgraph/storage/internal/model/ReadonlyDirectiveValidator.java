package com.circumgraph.storage.internal.model;

import java.util.function.Consumer;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.model.validation.ValidationMessage;

/**
 * Validator for the {@code readonly} directive which disables mutations for
 * a property.
 */
public class ReadonlyDirectiveValidator
	implements DirectiveValidator<FieldDef>
{
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
	public void validate(
		FieldDef location,
		DirectiveUse directive,
		Consumer<ValidationMessage> validationCollector
	)
	{
		if(directive.getArguments().isEmpty()) return;

		validationCollector.accept(ValidationMessage.error()
			.withLocation(directive.getSourceLocation())
			.withMessage("@readonly does not support arguments")
			.withCode("storage:readonly-arguments-invalid")
			.build()
		);
	}
}

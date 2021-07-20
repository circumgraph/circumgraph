package com.circumgraph.storage.internal.model;

import java.util.function.Consumer;

import com.circumgraph.model.ArgumentUse;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.internal.ValueProviders;

/**
 * Validator for the {@code default} directive which is used to provide
 * default values.
 *
 * <pre>
 * id: ID @default(provider: ID)
 * name: String @default(value: "Test Name")
 * </pre>
 */
public class DefaultDirectiveValidator
	implements DirectiveValidator<FieldDef>
{
	private final ValueProviders valueProviders;

	public DefaultDirectiveValidator(ValueProviders valueProviders)
	{
		this.valueProviders = valueProviders;
	}

	@Override
	public String getName()
	{
		return "default";
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
		if(directive.getArguments().isEmpty()
			|| ! DirectiveValidator.checkOnlyArguments(directive, "provider", "value"))
		{
			validationCollector.accept(ValidationMessage.error()
				.withLocation(directive.getSourceLocation())
				.withMessage("@default can only be used with one argument, please use either `provider` or `value`, but not both")
				.withCode("storage:default-arguments-invalid")
				.build()
			);

			return;
		}

		var provider = directive.getArgument("provider")
			.flatMap(ArgumentUse::getValueAsString);

		var value = directive.getArgument("value")
			.flatMap(ArgumentUse::getValueAsString);

		if(provider.isPresent())
		{
			// Using a provider - make sure its actually available
			var actualProvider = valueProviders.get(provider.get());
			if(! actualProvider.isPresent())
			{
				validationCollector.accept(ValidationMessage.error()
					.withLocation(directive.getSourceLocation())
					.withMessage("The provider `%s` is not available for @default", provider.get())
					.withCode("storage:default-provider-unknown")
					.build()
				);
			}
		}
		else if(value.isPresent())
		{
			// TODO: Validation for `value`
		}
		else
		{
			validationCollector.accept(ValidationMessage.error()
				.withLocation(directive.getSourceLocation())
				.withMessage("@default should be used with either `provider` or `value`")
				.withCode("storage:default-arguments-invalid")
				.build()
			);
		}
	}
}

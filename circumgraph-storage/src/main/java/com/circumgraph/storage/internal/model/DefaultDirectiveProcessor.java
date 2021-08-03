package com.circumgraph.storage.internal.model;

import java.util.function.Consumer;

import com.circumgraph.model.ArgumentUse;
import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;
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
public class DefaultDirectiveProcessor
	implements DirectiveUseProcessor<FieldDef>
{
	private static final ValidationMessageType INVALID_ARGUMENTS = ValidationMessageType.error()
		.withCode("storage:@default:invalid-arguments")
		.withMessage("@default requires either provider or value to be provided")
		.build();

	private static final ValidationMessageType UNKNOWN_PROVIDER = ValidationMessageType.error()
		.withCode("storage:@default:unknown-provider")
		.withMessage("The provider {{provider}} is not available")
		.withArgument("provider")
		.build();

	private static final ValidationMessageType INVALID_PROVIDER = ValidationMessageType.error()
		.withCode("storage:@default:invalid-provider")
		.withMessage("{{provider}} does not support the type {{fieldType}}")
		.withArgument("provider")
		.withArgument("fieldType")
		.build();

	private final ValueProviders valueProviders;

	public DefaultDirectiveProcessor(ValueProviders valueProviders)
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
	public void process(
		FieldDef location,
		DirectiveUse directive,
		Consumer<ValidationMessage> validationCollector
	)
	{
		if(directive.getArguments().isEmpty()
			|| ! DirectiveUseProcessor.checkOnlyArguments(directive, "provider", "value"))
		{
			validationCollector.accept(INVALID_ARGUMENTS
				.toMessage()
				.withLocation(directive.getSourceLocation())
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
				validationCollector.accept(UNKNOWN_PROVIDER.toMessage()
					.withLocation(directive.getSourceLocation())
					.withArgument("provider", provider.get())
					.build()
				);
			}
			else
			{
				var instance = actualProvider.get();
				if(location.getType().isAssignableFrom(instance.getType()))
				{
					validationCollector.accept(INVALID_PROVIDER.toMessage()
						.withLocation(directive.getSourceLocation())
						.withArgument("provider", provider.get())
						.withArgument("fieldType", location.getTypeName())
						.build()
					);
				}
			}
		}
		else if(value.isPresent())
		{
			// TODO: Validation for `value`
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

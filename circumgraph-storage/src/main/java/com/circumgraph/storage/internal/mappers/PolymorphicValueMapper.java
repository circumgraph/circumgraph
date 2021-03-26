package com.circumgraph.storage.internal.mappers;

import java.util.function.Consumer;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.StructuredValue;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.MapIterable;

/**
 * Mapper that can handle polymorphism of types. Will delegate to instances
 * of {@link StructuredValueMapper}.
 */
public class PolymorphicValueMapper
	implements ValueMapper<StructuredValue, StructuredMutation>
{
	private final StructuredDef type;
	private final MapIterable<String, StructuredValueMapper> subTypes;
	private final ListIterable<ValueValidator<StructuredValue>> validators;

	public PolymorphicValueMapper(
		StructuredDef type,
		MapIterable<String, StructuredValueMapper> subTypes,
		ListIterable<ValueValidator<StructuredValue>> validators
	)
	{
		this.type = type;
		this.subTypes = subTypes;
		this.validators = validators;
	}

	@Override
	public StructuredValue getInitialValue()
	{
		return null;
	}

	@Override
	public StructuredValue applyMutation(
		StructuredValue previousValue,
		StructuredMutation mutation
	)
	{
		StructuredValueMapper mapper = subTypes.get(mutation.getType().getName());
		if(mapper == null)
		{
			// Create an empty value with the main type, will fail validation
			return StructuredValue.create(type, Maps.immutable.empty());
		}

		return mapper.applyMutation(
			previousValue != null && previousValue.getDefinition() == mutation.getType()
				? previousValue
				: null,
			mutation
		);
	}

	@Override
	public void validate(
		Consumer<ValidationMessage> validationCollector,
		StructuredValue value
	)
	{
		StructuredValueMapper mapper = subTypes.get(value.getDefinition().getName());
		if(mapper == null)
		{
			validationCollector.accept(ValidationMessage.error()
				.withMessage("Type %s not supported", value.getDefinition().getName())
				.build()
			);
		}
		else
		{
			// Ask the subtype to validate
			mapper.validate(validationCollector, value);

			// Run through all of the other validators
			for(ValueValidator<StructuredValue> v : validators)
			{
				v.validate(value, validationCollector);
			}
		}
	}
}

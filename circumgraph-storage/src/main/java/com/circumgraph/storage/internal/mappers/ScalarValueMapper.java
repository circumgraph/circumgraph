package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.validation.ValidationMessageCollector;
import com.circumgraph.storage.mutation.SimpleValueMutation;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.SimpleValue;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Mapper for scalar values.
 */
public class ScalarValueMapper
	implements ValueMapper<SimpleValue, SimpleValueMutation<?>>
{
	private final ScalarDef typeDef;
	private final Object defaultValue;

	private final ListIterable<ValueValidator<SimpleValue>> validators;

	public ScalarValueMapper(
		ScalarDef typeDef,
		Object defaultValue,
		ListIterable<ValueValidator<SimpleValue>> validators
	)
	{
		this.typeDef = typeDef;
		this.defaultValue = defaultValue;
		this.validators = validators;
	}

	@Override
	public SimpleValue getInitialValue()
	{
		return SimpleValue.create(typeDef, defaultValue);
	}

	@Override
	public SimpleValue applyMutation(
		SimpleValue previousValue,
		SimpleValueMutation<?> mutation
	)
	{
		return SimpleValue.create(typeDef, mutation.getValue());
	}

	@Override
	public void validate(
		ValidationMessageCollector collector,
		SimpleValue value
	)
	{
		for(ValueValidator<SimpleValue> v : validators)
		{
			v.validate(value, collector);
		}
	}
}

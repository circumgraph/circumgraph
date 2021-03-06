package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.validation.ValidationMessageCollector;
import com.circumgraph.storage.StorageException;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.StructuredValue;
import com.circumgraph.values.Value;
import com.circumgraph.values.internal.StructuredValueImpl;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;

/**
 * Mapper for {@link StructuredValue}.
 */
public class StructuredValueMapper
	implements ValueMapper<StructuredValue, StructuredMutation>
{
	private final StructuredDef type;
	private final MapIterable<String, ValueMapper<?, ?>> fields;
	private final ListIterable<ValueValidator<StructuredValue>> validators;

	public StructuredValueMapper(
		StructuredDef type,
		MapIterable<String, ValueMapper<?, ?>> fields,
		ListIterable<ValueValidator<StructuredValue>> validators
	)
	{
		this.type = type;
		this.fields = fields;
		this.validators = validators;
	}

	@Override
	public StructuredValue getInitialValue()
	{
		MutableMap<String, Value> values = Maps.mutable.withInitialCapacity(
			fields.size()
		);

		fields.forEachKeyValue((key, mapper) -> {
			Value value = mapper.getInitialValue();
			if(value != null)
			{
				values.put(key, value);
			}
		});

		return new StructuredValueImpl(type, values);
	}

	@Override
	public StructuredValue applyMutation(
		StructuredValue previousValue,
		StructuredMutation mutation
	)
	{
		boolean hasPreviousValue = previousValue != null;

		MutableMap<String, Value> values = hasPreviousValue
			? Maps.mutable.ofMapIterable(previousValue.getFields())
			: Maps.mutable.ofInitialCapacity(fields.size());

		mutation.getFields().forEachKeyValue((key, fieldMutation) -> {
			Value value;
			var mapper = (ValueMapper) fields.get(key);
			if(mapper == null)
			{
				throw new StorageException("Unable to store, unknown field in input");
			}

			if(fieldMutation == null)
			{
				// No mutation, assume null
				if(hasPreviousValue)
				{
					value = null;
				}
				else
				{
					value = mapper.getInitialValue();
				}
			}
			else
			{
				value = mapper.applyMutation(
					values.get(key),
					fieldMutation
				);
			}

			if(value == null)
			{
				values.remove(key);
			}
			else
			{
				values.put(key, value);
			}
		});

		return new StructuredValueImpl(type, values);
	}

	@Override
	public void validate(
		ValidationMessageCollector collector,
		StructuredValue value
	)
	{
		var fields = value.getFields();

		this.fields.forEachKeyValue((key, mapper) -> {
			((ValueMapper) mapper).validate(collector, fields.get(key));
		});

		for(ValueValidator<StructuredValue> v : validators)
		{
			v.validate(value, collector);
		}
	}
}

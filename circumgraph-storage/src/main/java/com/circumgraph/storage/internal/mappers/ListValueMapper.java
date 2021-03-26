package com.circumgraph.storage.internal.mappers;

import java.util.function.Consumer;

import com.circumgraph.model.ListDef;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.storage.mutation.ListMutation;
import com.circumgraph.storage.mutation.ListSetMutation;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.ListValue;
import com.circumgraph.values.Value;

import org.eclipse.collections.api.list.ListIterable;

public class ListValueMapper<V extends Value>
	implements ValueMapper<ListValue<V>, ListMutation<V>>
{
	private final ListDef typeDef;

	private final ListIterable<ValueValidator<ListValue<V>>> validators;
	private final ValueMapper<V, ?> itemMapper;

	public ListValueMapper(
		ListDef typeDef,
		ListIterable<ValueValidator<ListValue<V>>> validators,
		ValueMapper<V, ?> itemMapper
	)
	{
		this.typeDef = typeDef;
		this.validators = validators;
		this.itemMapper = itemMapper;
	}

	@Override
	public ListValue<V> getInitialValue()
	{
		return null;
	}

	@Override
	public ListValue<V> applyMutation(
		ListValue<V> previousValue,
		ListMutation<V> mutation
	)
	{
		if(mutation instanceof ListSetMutation)
		{
			var casted = (ListSetMutation<V>) mutation;
			return ListValue.create(
				typeDef,
				casted.getValues()
			);
		}

		return previousValue;
	}

	@Override
	public void validate(
		Consumer<ValidationMessage> validationCollector,
		ListValue<V> value
	)
	{
		value.items()
			.forEach(v -> itemMapper.validate(validationCollector, v));

		for(ValueValidator<ListValue<V>> v : validators)
		{
			v.validate(value, validationCollector);
		}
	}
}

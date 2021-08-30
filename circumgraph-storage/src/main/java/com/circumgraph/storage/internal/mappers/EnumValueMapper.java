package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.ObjectLocation;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.mutation.SetEnumValueMutation;
import com.circumgraph.storage.types.ValueMapper;

import org.eclipse.collections.api.set.ImmutableSet;

import reactor.core.publisher.Mono;

/**
 * Mapper of {@link EnumDef} to a {@link SimpleValue}.
 */
public class EnumValueMapper
	implements ValueMapper<SimpleValue, SetEnumValueMutation>
{
	private static ValidationMessageType ERROR = ValidationMessageType.error()
		.withCode("storage:validation:invalid-enum-value")
		.withArgument("value")
		.withArgument("enum")
		.withMessage("The value `{{value}}` is not valid for the enum `{{enum}}`")
		.build();

	private final EnumDef def;
	private final ImmutableSet<String> values;

	public EnumValueMapper(
		EnumDef def
	)
	{
		this.def = def;

		this.values = def.getValues()
			.collect(EnumValueDef::getName)
			.toSet()
			.toImmutable();
	}

	@Override
	public OutputTypeDef getDef()
	{
		return def;
	}

	@Override
	public Mono<SimpleValue> applyMutation(
		MappingEncounter encounter,
		ObjectLocation location,
		SimpleValue previousValue,
		SetEnumValueMutation mutation
	)
	{
		return Mono.defer(() -> {
			var value = SimpleValue.create(def, mutation.getValue());
			if(value != null && ! values.contains(value.get()))
			{
				// Invalid valid, report error
				encounter.reportError(ERROR.toMessage()
					.withLocation(location)
					.withArgument("value", value.get())
					.withArgument("enum", def.getName())
					.build());
			}

			return Mono.just(value);
		});
	}
}

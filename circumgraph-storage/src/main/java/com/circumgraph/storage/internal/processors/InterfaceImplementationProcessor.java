package com.circumgraph.storage.internal.processors;

import java.util.function.Consumer;

import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.processing.TypeDefProcessor;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.StorageSchema;

/**
 * Processor that verifies that all interfaces reachable from an entity have
 * at least one implementation available.
 */
public class InterfaceImplementationProcessor
	implements TypeDefProcessor<StructuredDef>
{
	private static final MetadataKey<Boolean> HAS_PROCESSED = MetadataKey.create("storage:interface-has-processed", Boolean.class);

	private static final ValidationMessageType IMPL_REQUIRED = ValidationMessageType.error()
		.withCode("storage:interface:implementation-required")
		.withArgument("type")
		.withMessage("{{type}} requires an implementation")
		.build();

	@Override
	public Class<StructuredDef> getType()
	{
		return StructuredDef.class;
	}

	@Override
	public void process(
		StructuredDef type,
		Consumer<ValidationMessage> validationCollector
	)
	{
		// Only process starting with entities
		if(! type.findImplements(StorageSchema.ENTITY_NAME)) return;

		processType(type, validationCollector);
	}

	private void processType(
		StructuredDef type,
		Consumer<ValidationMessage> validationCollector
	)
	{
		if(type.getMetadata(HAS_PROCESSED).orElse(false))
		{
			// If this is processed, skip it
			return;
		}
		// Mark as processed
		type.setMetadata(HAS_PROCESSED, true);

		for(var field : type.getFields())
		{
			var fieldType = field.getType();
			if(fieldType instanceof NonNullDef.Output n)
			{
				fieldType = n.getType();
			}

			if(fieldType instanceof ListDef.Output l)
			{
				fieldType = l.getItemType();

				if(fieldType instanceof NonNullDef.Output n)
				{
					fieldType = n.getType();
				}
			}

			if(fieldType instanceof StructuredDef s)
			{
				processType(s, validationCollector);
			}
		}

		// Process if this is an interface
		if(type instanceof InterfaceDef i)
		{
			processInterface(i, validationCollector);
		}
	}

	private void processInterface(
		InterfaceDef type,
		Consumer<ValidationMessage> validationCollector
	)
	{
		var types = type.getImplementors();
		if(types.isEmpty())
		{
			validationCollector.accept(IMPL_REQUIRED.toMessage()
				.withLocation(type)
				.withArgument("type", type.getName())
				.build()
			);
		}
		else
		{
			for(var subType : types)
			{
				processType(subType, validationCollector);
			}
		}
	}
}

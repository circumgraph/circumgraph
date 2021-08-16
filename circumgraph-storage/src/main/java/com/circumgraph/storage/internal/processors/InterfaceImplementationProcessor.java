package com.circumgraph.storage.internal.processors;

import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.processing.TypeDefProcessor;
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
		ProcessingEncounter encounter,
		StructuredDef type
	)
	{
		// Only process starting with entities
		if(! type.findImplements(StorageSchema.ENTITY_NAME)) return;

		processType(encounter, type);
	}

	private void processType(
		ProcessingEncounter encounter,
		StructuredDef type
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
				processType(encounter, s);
			}
		}

		// Process if this is an interface
		if(type instanceof InterfaceDef i)
		{
			processInterface(encounter, i);
		}
	}

	private void processInterface(
		ProcessingEncounter encounter,
		InterfaceDef type
	)
	{
		var types = type.getImplementors();
		if(types.isEmpty())
		{
			encounter.report(IMPL_REQUIRED.toMessage()
				.withLocation(type)
				.withArgument("type", type.getName())
				.build()
			);
		}
		else
		{
			for(var subType : types)
			{
				processType(encounter, subType);
			}
		}
	}
}

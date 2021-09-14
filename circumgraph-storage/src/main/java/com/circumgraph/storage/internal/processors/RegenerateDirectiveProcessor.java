package com.circumgraph.storage.internal.processors;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.StorageModel;

public class RegenerateDirectiveProcessor
	implements DirectiveUseProcessor<FieldDef>
{
	private static final ValidationMessageType INVALID_ARGUMENTS = ValidationMessageType.error()
		.withCode("storage:@regenerate:invalid-arguments")
		.withMessage("@regenerate does not support arguments")
		.build();

	private static final ValidationMessageType NOT_GENERATED = ValidationMessageType.error()
		.withCode("storage:@regenerate:not-generated")
		.withMessage("Field is a scalar or enum, but is not using @autoGenerated")
		.build();

	private static final ValidationMessageType UNSUPPORTED_TYPE = ValidationMessageType.error()
		.withCode("storage:@regenerate:unsupported-type")
		.withMessage("@regenerate can only be used with auto-generated scalars/enums or with objects")
		.build();

	@Override
	public String getName()
	{
		return "regenerate";
	}

	@Override
	public Location getLocation()
	{
		return StorageModel.LOCATION;
	}

	@Override
	public Class<FieldDef> getContextType()
	{
		return FieldDef.class;
	}

	@Override
	public void process(
		ProcessingEncounter encounter,
		FieldDef location,
		DirectiveUse directive
	)
	{
		if(directive.getArguments().isEmpty())
		{
			var type = location.getType();
			if(type instanceof NonNullDef.Output nonNullDef)
			{
				type = nonNullDef.getType();
			}

			if(type instanceof SimpleValueDef)
			{
				// Simple values need to use @autoGenerated
				if(StorageModel.getFieldMutation(location) != StorageModel.MutationType.NEVER)
				{
					encounter.report(NOT_GENERATED.toMessage()
						.withLocation(directive)
						.build()
					);
				}
				else
				{
					StorageModel.setRegenerateOnMutate(location, true);
				}
			}
			else if(type instanceof StructuredDef)
			{
				StorageModel.setRegenerateOnMutate(location, true);
			}
			else
			{
				encounter.report(UNSUPPORTED_TYPE.toMessage()
					.withLocation(directive)
					.build()
				);
			}
		}
		else
		{
			encounter.report(INVALID_ARGUMENTS.toMessage()
				.withLocation(directive.getDefinedAt())
				.build()
			);
		}
	}
}

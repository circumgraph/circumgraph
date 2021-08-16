package com.circumgraph.graphql.internal;

import com.circumgraph.graphql.FieldResolverFactory;
import com.circumgraph.graphql.GraphQLModel;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.processing.TypeDefProcessor;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.StorageModel;

/**
 * Processor that validates that all fields that are {@link StorageModel.FieldType#DYNAMIC}
 * have an associated {@link FieldResolverFactory}.
 */
public class DynamicFieldProcessor
	implements TypeDefProcessor<StructuredDef>
{
	private static final ValidationMessageType NO_RESOLVER = ValidationMessageType.error()
		.withCode("graphql:field:no-resolver")
		.withArgument("field")
		.withArgument("type")
		.withMessage("The field `{{field}}` in `{{type}}` has arguments but no resolver has been set")
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
		for(var field : type.getDirectFields())
		{
			if(StorageModel.getFieldType(field) == StorageModel.FieldType.DYNAMIC
				&& GraphQLModel.getFieldResolverFactory(field).isEmpty())
			{
				// Dynamic field without a resolver - report an error
				encounter.report(NO_RESOLVER.toMessage()
					.withLocation(field)
					.withArgument("field", field.getName())
					.withArgument("type", type.getName())
					.build()
				);
			}
		}
	}
}

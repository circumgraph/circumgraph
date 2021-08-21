package com.circumgraph.graphql.internal.processors;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.GraphQLModel;
import com.circumgraph.graphql.internal.resolvers.StaticValueFieldResolver;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.processing.TypeDefProcessor;
import com.circumgraph.model.validation.ValidationMessageType;
import com.circumgraph.storage.StorageModel;

/**
 * Processor that sets {@link FieldResolver}s for static fields in
 * {@link StructuredDef}.
 */
public class OutputProcessor
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
	public void process(ProcessingEncounter encounter, StructuredDef type)
	{
		for(var field : type.getDirectFields())
		{
			if(GraphQLModel.getFieldResolverFactory(field).isPresent()) continue;

			switch(StorageModel.getFieldType(field))
			{
				case STORED:
					// Set the resolver to use for static fields
					field.setRuntimeMetadata(GraphQLModel.FIELD_RESOLVER, new StaticValueFieldResolver(field.getName()));
					break;
				case DYNAMIC:
					// Report an error as this field can not be resolved
					encounter.report(NO_RESOLVER.toMessage()
						.withLocation(field)
						.withArgument("field", field.getName())
						.withArgument("type", type.getName())
						.build()
					);
					break;
			}
		}
	}
}

package com.circumgraph.graphql.internal.processors;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.GraphQLModel;
import com.circumgraph.graphql.internal.SchemaNames;
import com.circumgraph.graphql.internal.resolvers.RootQueryResolverFactory;
import com.circumgraph.graphql.internal.search.SearchQueryGenerator;
import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.processing.TypeDefProcessor;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StorageSchema;

/**
 * {@link TypeDefProcessor} responsible for dynamically generating the Query
 * object associated with entities.
 */
public class QueryProcessor
	implements TypeDefProcessor<StructuredDef>
{
	private static final FieldResolver GET_BY_ID = env -> {
		Collection collection = env.getSource();
		return collection.get(env.getArgument("id"));
	};

	private final SearchQueryGenerator generator;

	public QueryProcessor(SearchQueryGenerator generator)
	{
		this.generator = generator;
	}

	@Override
	public Location getLocation()
	{
		return GraphQLModel.LOCATION;
	}

	@Override
	public Class<StructuredDef> getType()
	{
		return StructuredDef.class;
	}

	@Override
	public void process(ProcessingEncounter encounter, StructuredDef type)
	{
		if(! type.hasImplements(StorageSchema.ENTITY_NAME)) return;

		var queryName = SchemaNames.toQueryObjectName(type);
		var queryType = ObjectDef.create(queryName)
			.withDescription("Query for " + type.getName())
			.addField(FieldDef.create("get")
				.withType(type)
				.withDescription("Get an object based on its identifier")
				.addArgument(ArgumentDef.create("id")
					.withType(NonNullDef.input(ScalarDef.ID))
					.withDescription("Identifier of object")
					.build()
				)
				.withMetadata(GraphQLModel.FIELD_RESOLVER, GET_BY_ID)
				.build()
			)
			.addField(generator.generateSearchQuery(type, "search"))
			.build();

		encounter.addType(queryType);

		// Make it reachable from the root query
		encounter.addType(ObjectDef.create("Query")
			.addField(FieldDef.create(SchemaNames.toQueryFieldName(type))
				.withType(queryType)
				.withDescription("Query for " + type.getName())
				.withMetadata(GraphQLModel.FIELD_RESOLVER_FACTORY, new RootQueryResolverFactory(type))
				.build()
			)
			.build()
		);
	}
}

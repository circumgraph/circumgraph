package com.circumgraph.graphql;

import com.circumgraph.graphql.internal.InterfaceResolver;
import com.circumgraph.graphql.internal.SchemaNames;
import com.circumgraph.graphql.internal.TransactionInstrumentation;
import com.circumgraph.graphql.internal.datafetchers.CollectionDeleteMutation;
import com.circumgraph.graphql.internal.datafetchers.CollectionGetByIdFetcher;
import com.circumgraph.graphql.internal.datafetchers.CollectionStoreMutation;
import com.circumgraph.graphql.internal.datafetchers.StructuredValueDataFetcher;
import com.circumgraph.graphql.internal.mutation.ListMutationMapper;
import com.circumgraph.graphql.internal.mutation.PolymorphicMutationMapper;
import com.circumgraph.graphql.internal.mutation.ScalarMutationMapper;
import com.circumgraph.graphql.internal.mutation.StoredObjectRefMutationMapper;
import com.circumgraph.graphql.internal.mutation.StructuredValueMutationMapper;
import com.circumgraph.graphql.internal.output.ListOutputMapper;
import com.circumgraph.graphql.internal.output.NonNullOutputMapper;
import com.circumgraph.graphql.internal.output.StoredObjectRefOutputMapper;
import com.circumgraph.graphql.internal.output.StructuredValueOutputMapper;
import com.circumgraph.graphql.internal.scalars.BooleanScalar;
import com.circumgraph.graphql.internal.scalars.FloatScalar;
import com.circumgraph.graphql.internal.scalars.IDScalar;
import com.circumgraph.graphql.internal.scalars.IntScalar;
import com.circumgraph.graphql.internal.scalars.StringScalar;
import com.circumgraph.graphql.internal.search.SearchQueryGenerator;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StorageSchema;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;
import se.l4.ylem.ids.Base62LongIdCodec;
import se.l4.ylem.ids.LongIdCodec;

public class GraphQLGenerator
{
	private final Storage storage;
	private final DataFetcher<?> entityGet;

	private final ImmutableMap<ScalarDef, ScalarMapper<?>> scalars;
	private final LongIdCodec<String> idCodec;

	private final MutableMap<OutputTypeDef, MutationInputMapper<?>> mutationMappers;

	public GraphQLGenerator(Storage storage)
	{
		this.storage = storage;

		idCodec = new Base62LongIdCodec();
		entityGet = new CollectionGetByIdFetcher(idCodec);

		// TODO: Allow dynamic loading of scalars
		scalars = Maps.immutable.<ScalarDef, ScalarMapper<?>>empty()
			.newWithKeyValue(ScalarDef.BOOLEAN, new BooleanScalar())
			.newWithKeyValue(ScalarDef.FLOAT, new FloatScalar())
			.newWithKeyValue(ScalarDef.ID, new IDScalar(idCodec))
			.newWithKeyValue(ScalarDef.INT, new IntScalar())
			.newWithKeyValue(ScalarDef.STRING, new StringScalar());

		mutationMappers = Maps.mutable.empty();
	}

	public GraphQL.Builder generate()
	{
		return generate(generateSchema());
	}

	public GraphQL.Builder generate(GraphQLSchema schema)
	{
		// TODO: Error mapping
		return GraphQL.newGraphQL(schema)
			.doNotAddDefaultInstrumentations()
			.instrumentation(new TransactionInstrumentation(storage));
	}

	public GraphQLSchema generateSchema()
	{
		GraphQLSchema.Builder schema = GraphQLSchema.newSchema();
		GraphQLCodeRegistry.Builder registry = GraphQLCodeRegistry.newCodeRegistry();

		Model model = storage.getModel();

		for(TypeDef type : model.getTypes())
		{
			generateType(type, schema, registry);
		}

		// Type returned when deleting something
		schema.additionalType(GraphQLObjectType.newObject()
			.name("DeleteResult")
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("success")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLBoolean))
			)
			.build()
		);

		registry.dataFetcher(
			FieldCoordinates.coordinates("DeleteResult", "success"),
			(DataFetchingEnvironment env) -> env.getContext()
		);

		var searchGenerator = new SearchQueryGenerator(registry, idCodec);

		// Generate the query and mutation root objects
		GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
			.name("Query");
		GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject()
			.name("Mutation");

		for(var collection : storage.getCollections())
		{
			generateQuery(collection, schema, registry, searchGenerator, queryBuilder);
			generateMutation(collection, schema, registry, mutationBuilder);
		}

		return schema
			.query(queryBuilder)
			.mutation(mutationBuilder)
			.codeRegistry(registry.build())
			.build();
	}

	private void generateType(
		TypeDef t,
		GraphQLSchema.Builder schema,
		GraphQLCodeRegistry.Builder registry
	)
	{
		if(t instanceof ScalarDef)
		{
			var scalar = scalars.get(t);
			if(scalar == null)
			{
				throw new ModelException("Unsupported scalar: " + t.getName());
			}
		}
		else if(t instanceof ObjectDef)
		{
			ObjectDef def = (ObjectDef) t;

			GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
				.name(def.getName())
				.description(def.getDescription().orElse(null));

			for(FieldDef field : def.getFields())
			{
				var fieldMapper = resolveOutputType(field.getType());

				GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
					.name(field.getName())
					.description(field.getDescription().orElse(null))
					.type(fieldMapper.getGraphQLType())
					.build();

				builder.field(fieldDef);

				registry.dataFetcher(
					FieldCoordinates.coordinates(def.getName(), field.getName()),
					new StructuredValueDataFetcher(
						field.getName(),
						fieldMapper
					)
				);
			}

			for(var i : def.getAllImplements())
			{
				builder.withInterface(new GraphQLTypeReference(i.getName()));
			}

			schema.additionalType(builder.build());
		}
		else if(t instanceof InterfaceDef)
		{
			InterfaceDef def = (InterfaceDef) t;

			GraphQLInterfaceType.Builder builder = GraphQLInterfaceType.newInterface()
				.name(def.getName())
				.description(def.getDescription().orElse(null));

			for(FieldDef field : def.getFields())
			{
				var fieldMapper = resolveOutputType(field.getType());

				GraphQLFieldDefinition fieldDef = GraphQLFieldDefinition.newFieldDefinition()
					.name(field.getName())
					.description(field.getDescription().orElse(null))
					.type(fieldMapper.getGraphQLType())
					.build();

				builder.field(fieldDef);
			}

			for(var i : def.getAllImplements())
			{
				builder.withInterface(new GraphQLTypeReference(i.getName()));
			}

			GraphQLInterfaceType type = builder.build();
			schema.additionalType(type);

			registry.typeResolver(type, new InterfaceResolver());
		}
		else if(t instanceof UnionDef)
		{
			var def = (UnionDef) t;

			GraphQLUnionType.Builder builder = GraphQLUnionType.newUnionType()
				.name(def.getName())
				.description(def.getDescription().orElse(null));

			for(var subDef : def.getTypes())
			{
				builder.possibleType(GraphQLTypeReference.typeRef(subDef.getName()));
			}

			var type = builder.build();
			schema.additionalType(type);
			registry.typeResolver(type, new InterfaceResolver());
		}
	}

	private OutputMapper<?, ?> resolveOutputType(OutputTypeDef type)
	{
		if(type instanceof NonNullDef.Output)
		{
			return new NonNullOutputMapper<>(
				resolveOutputType(((NonNullDef.Output) type).getType())
			);
		}
		else if(type instanceof ListDef.Output)
		{
			var listDef = (ListDef.Output) type;
			return new ListOutputMapper(
				listDef,
				resolveOutputType(listDef.getItemType())
			);
		}
		else if(type instanceof ScalarDef)
		{
			var scalarDef = (ScalarDef) type;
			return scalars.get(scalarDef);
		}
		else if(type instanceof StructuredDef)
		{
			var structuredDef = (StructuredDef) type;
			if(structuredDef.findImplements(StorageSchema.ENTITY_NAME))
			{
				// Reference to another entity
				return new StoredObjectRefOutputMapper(
					storage.get(structuredDef.getName())
				);
			}

			return new StructuredValueOutputMapper(structuredDef);
		}
		else if(type instanceof UnionDef)
		{
			var unionDef = (UnionDef) type;
			return new StructuredValueOutputMapper(unionDef);
		}

		throw new ModelException("Can not map the given type to GraphQL: " + type);
	}

	private void generateQuery(
		Collection entity,
		GraphQLSchema.Builder schema,
		GraphQLCodeRegistry.Builder registry,
		SearchQueryGenerator searchQuery,
		GraphQLObjectType.Builder queryBuilder
	)
	{
		String queryObjectName = SchemaNames.toQueryObjectName(entity.getDefinition());
		String queryFieldName = SchemaNames.toQueryFieldName(entity.getDefinition());

		GraphQLObjectType.Builder query = GraphQLObjectType.newObject()
			.name(queryObjectName)
			.field(GraphQLFieldDefinition.newFieldDefinition()
				.name("get")
				.type(GraphQLTypeReference.typeRef(entity.getDefinition().getName()))
				.argument(GraphQLArgument.newArgument()
					.name("id")
					.type(GraphQLNonNull.nonNull(Scalars.GraphQLID))
				)
				.build()
			);

		searchQuery.generateSearchQuery(entity, queryObjectName, query);

		registry.dataFetcher(
			FieldCoordinates.coordinates(queryObjectName, "get"),
			entityGet
		);

		// Generate the query field
		queryBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
			.name(queryFieldName)
			.type(query)
			.build()
		);

		registry.dataFetcher(
			FieldCoordinates.coordinates("Query", queryFieldName),
			(DataFetcher) env -> entity
		);
	}

	private void generateMutation(
		Collection entity,
		GraphQLSchema.Builder schema,
		GraphQLCodeRegistry.Builder registry,
		GraphQLObjectType.Builder mutationBuilder
	)
	{
		var mapper = (MutationInputMapper) generateStructuredMutationInput(entity.getDefinition(), false);
		var name = entity.getDefinition().getName();

		mutationBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
			.name("store" + name)
			.description("Create or update an object of type " + name)
			.type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef(entity.getDefinition().getName())))
			.argument(GraphQLArgument.newArgument()
				.name("id")
				.description("Identifier of object to update, no id will store a new object")
				.type(Scalars.GraphQLID)
			)
			.argument(GraphQLArgument.newArgument()
				.name("mutation")
				.description("Mutation to apply")
				.type(GraphQLNonNull.nonNull(mapper.getGraphQLType()))
			)
		);

		registry.dataFetcher(
			FieldCoordinates.coordinates("Mutation", "store" + name),
			new CollectionStoreMutation(entity, idCodec, mapper)
		);

		mutationBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
			.name("delete" + name)
			.description("Delete an object of type " + name)
			.type(GraphQLTypeReference.typeRef("DeleteResult"))
			.argument(GraphQLArgument.newArgument()
				.name("id")
				.description("Identifier of object to delete")
				.type(GraphQLNonNull.nonNull(Scalars.GraphQLID))
			)
		);

		registry.dataFetcher(
			FieldCoordinates.coordinates("Mutation", "delete" + name),
			new CollectionDeleteMutation(entity, idCodec)
		);
	}

	private MutationInputMapper<?> generateMutationInput(OutputTypeDef def)
	{
		if(def instanceof NonNullDef.Output)
		{
			// Unwrap null values
			def = ((NonNullDef.Output) def).getType();
		}

		// Check if this mutation mapper has already been generated
		var mapper = mutationMappers.get(def);
		if(mapper != null)
		{
			return mapper;
		}

		if(def instanceof ScalarDef)
		{
			mapper = new ScalarMutationMapper((ScalarMapper) scalars.get(def));
		}
		else if(def instanceof StructuredDef)
		{
			mapper = generateStructuredMutationInput((StructuredDef) def, true);
		}
		else if(def instanceof ListDef.Output)
		{
			var listDef = (ListDef.Output) def;
			mapper = new ListMutationMapper(
				listDef,
				generateMutationInput(listDef.getItemType())
			);
		}
		else if(def instanceof UnionDef)
		{
			var unionDef = (UnionDef) def;
			mapper = new PolymorphicMutationMapper(
				unionDef,
				unionDef.getTypes().collect(subDef -> generateMutationInput(subDef))
			);
		}
		else
		{
			throw new ModelException("Unable to model " + def + " as an input type");
		}

		mutationMappers.put(def, mapper);
		return mapper;
	}

	private MutationInputMapper<?> generateStructuredMutationInput(
		StructuredDef def,
		boolean allowReferences
	)
	{
		if(allowReferences && def.findImplements(StorageSchema.ENTITY_NAME))
		{
			return generateReferenceInput(def);
		}

		if(def instanceof InterfaceDef)
		{
			var mappers = storage.getModel().getImplements(def.getName())
				.collect(d -> generateStructuredMutationInput(d, false));

			return new PolymorphicMutationMapper(def, mappers);
		}
		else if(def instanceof ObjectDef)
		{
			var objectDef = (ObjectDef) def;
			var fields = objectDef.getFields()
				.toMap(k -> k, k -> generateMutationInput(k.getType()));

			return new StructuredValueMutationMapper(objectDef, fields);
		}

		throw new ModelException("Unable to model " + def + " as an input type");
	}

	private MutationInputMapper<?> generateReferenceInput(StructuredDef def)
	{
		return new StoredObjectRefMutationMapper(idCodec, def);
	}
}

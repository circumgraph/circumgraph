package com.circumgraph.graphql;

import java.util.List;

import com.circumgraph.graphql.internal.DataFetcherExceptionHandlerImpl;
import com.circumgraph.graphql.internal.FieldResolverAdapter;
import com.circumgraph.graphql.internal.InterfaceResolver;
import com.circumgraph.graphql.internal.TransactionInstrumentation;
import com.circumgraph.graphql.scalars.BooleanScalar;
import com.circumgraph.graphql.scalars.DurationScalar;
import com.circumgraph.graphql.scalars.FloatScalar;
import com.circumgraph.graphql.scalars.IDScalar;
import com.circumgraph.graphql.scalars.IntScalar;
import com.circumgraph.graphql.scalars.LocalDateScalar;
import com.circumgraph.graphql.scalars.LocalDateTimeScalar;
import com.circumgraph.graphql.scalars.LocalTimeScalar;
import com.circumgraph.graphql.scalars.OffsetDateTimeScalar;
import com.circumgraph.graphql.scalars.StringScalar;
import com.circumgraph.graphql.scalars.ZonedDateTimeScalar;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InputObjectDef;
import com.circumgraph.model.InputTypeDef;
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
import com.circumgraph.storage.Storage;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;

import graphql.GraphQL;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;

public class GraphQLGenerator
{
	private final Storage storage;
	private final GraphQLCreationEncounter encounter;
	private final ImmutableMap<ScalarDef, ScalarMapper<?>> scalars;

	public GraphQLGenerator(Storage storage)
	{
		this.storage = storage;

		scalars = Maps.immutable.<ScalarDef, ScalarMapper<?>>empty()
			.newWithKeyValue(ScalarDef.BOOLEAN, new BooleanScalar())
			.newWithKeyValue(ScalarDef.FLOAT, new FloatScalar())
			.newWithKeyValue(ScalarDef.ID, new IDScalar())
			.newWithKeyValue(ScalarDef.INT, new IntScalar())
			.newWithKeyValue(ScalarDef.STRING, new StringScalar())
			.newWithKeyValue(ScalarDef.LOCAL_DATE, new LocalDateScalar())
			.newWithKeyValue(ScalarDef.LOCAL_TIME, new LocalTimeScalar())
			.newWithKeyValue(ScalarDef.LOCAL_DATE_TIME, new LocalDateTimeScalar())
			.newWithKeyValue(ScalarDef.OFFSET_DATE_TIME, new OffsetDateTimeScalar())
			.newWithKeyValue(ScalarDef.ZONED_DATE_TIME, new ZonedDateTimeScalar())
			.newWithKeyValue(ScalarDef.DURATION, new DurationScalar());

		encounter = new GraphQLCreationEncounter()
		{
			@Override
			public Storage getStorage()
			{
				return storage;
			}
		};
	}

	public GraphQL.Builder generate()
	{
		return generate(generateSchema());
	}

	public GraphQL.Builder generate(GraphQLSchema schema)
	{
		return GraphQL.newGraphQL(schema)
			.defaultDataFetcherExceptionHandler(new DataFetcherExceptionHandlerImpl())
			.doNotAddDefaultInstrumentations()
			.instrumentation(new TransactionInstrumentation(storage));
	}

	public GraphQLSchema generateSchema()
	{
		GraphQLSchema.Builder schema = GraphQLSchema.newSchema();
		GraphQLCodeRegistry.Builder registry = GraphQLCodeRegistry.newCodeRegistry();

		Model model = storage.getModel();

		for(var def : model.getTypes())
		{
			var type = generateType(registry, def);
			schema.additionalType(type);

			if(type instanceof GraphQLObjectType o)
			{
				if(o.getName().equals("Query"))
				{
					schema.query(o);
				}
				else if(o.getName().equals("Mutation"))
				{
					schema.mutation(o);
				}
			}
		}

		return schema
			.codeRegistry(registry.build())
			.build();
	}

	/**
	 * Generate and return {@link GraphQLType}.
	 *
	 * @param registry
	 *   registry used to register {@link graphql.schema.DataFetcher}s
	 * @param def
	 *   definition to generate from
	 */
	private GraphQLType generateType(
		GraphQLCodeRegistry.Builder registry,
		TypeDef def
	)
	{
		if(def instanceof ScalarDef)
		{
			var scalar = scalars.get(def);
			if(scalar == null)
			{
				throw new ModelException("Unsupported scalar: " + def.getName());
			}

			return scalar.getGraphQLType();
		}
		else if(def instanceof ObjectDef objectDef)
		{
			GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
				.name(objectDef.getName())
				.description(objectDef.getDescription().orElse(null))
				.fields(generateFields(registry, objectDef));

			for(var i : objectDef.getAllImplements())
			{
				builder.withInterface(new GraphQLTypeReference(i.getName()));
			}

			return builder.build();
		}
		else if(def instanceof InterfaceDef interfaceDef)
		{
			var builder = GraphQLInterfaceType.newInterface()
				.name(interfaceDef.getName())
				.description(interfaceDef.getDescription().orElse(null))
				.fields(generateFields(registry, interfaceDef));

			for(var i : interfaceDef.getAllImplements())
			{
				builder.withInterface(new GraphQLTypeReference(i.getName()));
			}

			GraphQLInterfaceType type = builder.build();
			registry.typeResolver(type, new InterfaceResolver());
			return type;
		}
		else if(def instanceof UnionDef unionDef)
		{
			var builder = GraphQLUnionType.newUnionType()
				.name(unionDef.getName())
				.description(unionDef.getDescription().orElse(null));

			for(var subDef : unionDef.getTypes())
			{
				builder.possibleType(GraphQLTypeReference.typeRef(subDef.getName()));
			}

			var type = builder.build();
			registry.typeResolver(type, new InterfaceResolver());
			return type;
		}
		else if(def instanceof EnumDef enumDef)
		{
			var builder = GraphQLEnumType.newEnum()
				.name(enumDef.getName())
				.description(enumDef.getDescription().orElse(null));

			for(var value : enumDef.getValues())
			{
				builder.value(GraphQLEnumValueDefinition.newEnumValueDefinition()
					.name(value.getName())
					.description(value.getDescription().orElse(null))
					.value(value.getMetadata(GraphQLModel.ENUM_VALUE).orElse(value.getName()))
					.build()
				);
			}

			return builder.build();
		}
		else if(def instanceof InputObjectDef inputObjectDef)
		{
			var builder = GraphQLInputObjectType.newInputObject()
				.name(inputObjectDef.getName())
				.description(inputObjectDef.getDescription().orElse(null));

			for(var field : inputObjectDef.getFields())
			{
				var fieldBuilder = GraphQLInputObjectField.newInputObjectField()
					.name(field.getName())
					.description(field.getDescription().orElse(null))
					.type(resolveInputType(field.getType()));

				if(field.getDefaultValue().isPresent())
				{
					fieldBuilder = fieldBuilder.defaultValueProgrammatic(field.getDefaultValue().get());
				}

				builder.field(fieldBuilder.build());
			}

			return builder.build();
		}
		else
		{
			throw new ModelException("Unsupported type encountered: " + def);
		}
	}

	/**
	 * Generate fields for a {@link StructuredDef}.
	 *
	 * @param registry
	 *   registry where {@link graphql.schema.DataFetcher} is registered
	 * @param structuredDef
	 *   definition to generate fields for
	 * @return
	 *   generated fields
	 */
	private List<GraphQLFieldDefinition> generateFields(
		GraphQLCodeRegistry.Builder registry,
		StructuredDef structuredDef
	)
	{
		var result = Lists.mutable.<GraphQLFieldDefinition>empty();

		for(var field : structuredDef.getFields())
		{
			var resolverFactory = GraphQLModel.getFieldResolverFactory(field).get();
			var actualResolver = resolverFactory.create(encounter);

			result.add(GraphQLFieldDefinition.newFieldDefinition()
				.name(field.getName())
				.description(field.getDescription().orElse(null))
				.type(resolveOutputType(field.getType()))
				.arguments(generateArguments(field))
				.build()
			);

			registry.dataFetcher(
				FieldCoordinates.coordinates(structuredDef.getName(), field.getName()),
				new FieldResolverAdapter(storage, actualResolver)
			);
		}

		return result;
	}

	private List<GraphQLArgument> generateArguments(
		FieldDef field
	)
	{
		var result = Lists.mutable.<GraphQLArgument>empty();

		for(var argument : field.getArguments())
		{
			var builder = GraphQLArgument.newArgument()
				.name(argument.getName())
				.description(argument.getDescription().orElse(null))
				.type(resolveInputType(argument.getType()));

			if(argument.getDefaultValue().isPresent())
			{
				builder = builder.defaultValueProgrammatic(argument.getDefaultValue().get());
			}

			result.add(builder.build());
		}

		return result;
	}

	private GraphQLOutputType resolveOutputType(OutputTypeDef def)
	{
		if(def instanceof NonNullDef.Output n)
		{
			return GraphQLNonNull.nonNull(resolveOutputType(n.getType()));
		}
		else if(def instanceof ListDef.Output l)
		{
			return GraphQLList.list(resolveOutputType(l.getItemType()));
		}

		return GraphQLTypeReference.typeRef(def.getName());
	}

	private GraphQLInputType resolveInputType(InputTypeDef def)
	{
		if(def instanceof NonNullDef.Input n)
		{
			return GraphQLNonNull.nonNull(resolveInputType(n.getType()));
		}
		else if(def instanceof ListDef.Input l)
		{
			return GraphQLList.list(resolveInputType(l.getItemType()));
		}

		return GraphQLTypeReference.typeRef(def.getName());
	}
}

package com.circumgraph.graphql.internal.processors;

import com.circumgraph.graphql.GraphQLModel;
import com.circumgraph.graphql.MutationInputMapper;
import com.circumgraph.graphql.ScalarMapper;
import com.circumgraph.graphql.internal.SchemaNames;
import com.circumgraph.graphql.internal.mutation.EnumMutationMapper;
import com.circumgraph.graphql.internal.mutation.ListMutationMapper;
import com.circumgraph.graphql.internal.mutation.PolymorphicMutationMapper;
import com.circumgraph.graphql.internal.mutation.ScalarMutationMapper;
import com.circumgraph.graphql.internal.mutation.StoredObjectRefMutationMapper;
import com.circumgraph.graphql.internal.mutation.StructuredValueMutationMapper;
import com.circumgraph.graphql.internal.resolvers.CollectionDeleteMutation;
import com.circumgraph.graphql.internal.resolvers.CollectionStoreMutation;
import com.circumgraph.graphql.scalars.BooleanScalar;
import com.circumgraph.graphql.scalars.FloatScalar;
import com.circumgraph.graphql.scalars.IDScalar;
import com.circumgraph.graphql.scalars.IntScalar;
import com.circumgraph.graphql.scalars.StringScalar;
import com.circumgraph.model.ArgumentDef;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.HasMetadata;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.ModelException;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.model.processing.ProcessingEncounter;
import com.circumgraph.model.processing.TypeDefProcessor;
import com.circumgraph.storage.StorageSchema;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;

/**
 * {@link TypeDefProcessor} that generates GraphQL mutations for every entity.
 */
public class MutationProcessor
	implements TypeDefProcessor<StructuredDef>
{
	@SuppressWarnings("rawtypes")
	private static final MetadataKey<MutationInputMapper> MAPPER =
		MetadataKey.create("graphql:mutation-mapper", MutationInputMapper.class);

	private final ImmutableMap<ScalarDef, ScalarMapper<?>> scalars;

	private static final ObjectDef DELETE_RESULT = ObjectDef.create("DeleteResult")
		.withDescription("Object used to signal if something was deleted")
		.addField(FieldDef.create("success")
			.withDescription("If an object was deleted")
			.withType(ScalarDef.BOOLEAN)
			.withMetadata(GraphQLModel.FIELD_RESOLVER, env -> true)
			.build()
		)
		.build();

	public MutationProcessor()
	{
		scalars = Maps.immutable.<ScalarDef, ScalarMapper<?>>empty()
			.newWithKeyValue(ScalarDef.BOOLEAN, new BooleanScalar())
			.newWithKeyValue(ScalarDef.FLOAT, new FloatScalar())
			.newWithKeyValue(ScalarDef.ID, new IDScalar())
			.newWithKeyValue(ScalarDef.INT, new IntScalar())
			.newWithKeyValue(ScalarDef.STRING, new StringScalar());
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

		// TODO: Check if mutation fields have been generated
		// TODO: Verify that this work if the StructuredDef is modified after this
		var mapper = (MutationInputMapper) generateStructuredMutationInput(type, false);
		var storeField = FieldDef.create(SchemaNames.toStoreMutationFieldName(type))
			.withType(NonNullDef.output(type))
			.withDescription("Create or update an object of type " + type.getName())
			.addArgument(ArgumentDef.create("id")
				.withType(ScalarDef.ID)
				.withDescription("Identifier of object to update, no id will create a new object")
				.build()
			)
			.addArgument(ArgumentDef.create("mutation")
				.withType(NonNullDef.input(mapper.getGraphQLType()))
				.withDescription("Mutation to apply to object")
				.build()
			)
			.withMetadata(GraphQLModel.FIELD_RESOLVER_FACTORY, new CollectionStoreMutation(mapper))
			.build();

		var deleteField = FieldDef.create(SchemaNames.toDeleteMutationFieldName(type))
			.withType(DELETE_RESULT)
			.addArgument(ArgumentDef.create("id")
				.withType(ScalarDef.ID)
				.withDescription("Identifier of object to delete")
				.build()
			)
			.withMetadata(GraphQLModel.FIELD_RESOLVER_FACTORY, new CollectionDeleteMutation(type))
			.build();

		encounter.addType(ObjectDef.create("Mutation")
			.addField(storeField)
			.addField(deleteField)
			.build()
		);
	}

	private MutationInputMapper<?> generateMutationInput(OutputTypeDef def)
	{
		if(def instanceof NonNullDef.Output n)
		{
			// Unwrap null values
			def = n.getType();
		}

		// Check if this mutation mapper has already been generated
		if(def instanceof HasMetadata m)
		{
			var mapper = m.getMetadata(MAPPER);
			if(mapper.isPresent())
			{
				return mapper.get();
			}
		}

		MutationInputMapper<?> mapper;
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
		else if(def instanceof EnumDef enumDef)
		{
			return new EnumMutationMapper(enumDef);
		}
		else
		{
			throw new ModelException("Unable to model " + def + " as an input type");
		}

		if(def instanceof HasMetadata m)
		{
			m.setRuntimeMetadata(MAPPER, mapper);
		}
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

		if(def instanceof InterfaceDef i)
		{
			var mappers =  i.getImplementors()
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
		return new StoredObjectRefMutationMapper(def);
	}
}

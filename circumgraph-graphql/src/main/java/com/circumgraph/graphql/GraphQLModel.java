package com.circumgraph.graphql;

import java.util.Optional;

import com.circumgraph.model.EnumValueDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.storage.StorageModel;

/**
 * Utilities for accessing and setting things related to the generated GraphQL
 * API.
 */
public class GraphQLModel
{
	/**
	 * Location used by the enhancements added by the GraphQL model.
	 */
	public static final Location LOCATION = Location.create("GraphQL API");

	/**
	 * Key for setting a {@link FieldResolver} on a built {@link FieldDef}.
	 *
	 * If used manually instead of via {@link #setFieldResolver(FieldDef, FieldResolver)}
	 * it may be required to set {@link StorageModel#FIELD_TYPE} to
	 * {@link StorageModel.FieldType#DYNAMIC}.
	 */
	public static final MetadataKey<FieldResolver> FIELD_RESOLVER =
		MetadataKey.create("graphql:field-resolver", FieldResolver.class);

	/**
	 * Key for setting a {@link FieldResolverFactory} on a built {@link FieldDef}.
	 *
	 * If used manually instead of via {@link #setFieldResolverFactory(FieldDef, FieldResolverFactory)}
	 * it may be required to set {@link StorageModel#FIELD_TYPE} to
	 * {@link StorageModel.FieldType#DYNAMIC}.
	 */
	public static final MetadataKey<FieldResolverFactory> FIELD_RESOLVER_FACTORY =
		MetadataKey.create("graphql:field-resolver-factory", FieldResolverFactory.class);

	/**
	 * Key for setting a custom Java-object to use as the value for an
	 * {@link EnumValueDef}.
	 */
	public static final MetadataKey<Object> ENUM_VALUE =
		MetadataKey.create("graphql:enum-value", Object.class);

	/**
	 * Get if a custom {@link FieldResolverFactory} has been set for the given
	 * field.
	 *
	 * @param field
	 *   field to get factory for
	 * @return
	 *   optional with {@link FieldResolverFactory} or empty optional
	 */
	public static Optional<FieldResolverFactory> getFieldResolverFactory(FieldDef field)
	{
		var factory = field.getMetadata(FIELD_RESOLVER_FACTORY);
		if(factory.isPresent())
		{
			return factory;
		}

		return field.getMetadata(FIELD_RESOLVER)
			.map(r -> (e -> r));
	}

	/**
	 * Set factory used to create {@link FieldResolver}s for the given field.
	 *
	 * @param field
	 * @param factory
	 */
	public static void setFieldResolverFactory(FieldDef field, FieldResolverFactory factory)
	{
		StorageModel.setType(field, StorageModel.FieldType.DYNAMIC);
		field.setRuntimeMetadata(FIELD_RESOLVER_FACTORY, factory);
	}

	/**
	 * Set the {@link FieldResolver} to use for the given field.
	 *
	 * @param field
	 * @param factory
	 */
	public static void setFieldResolver(FieldDef field, FieldResolver factory)
	{
		StorageModel.setType(field, StorageModel.FieldType.DYNAMIC);
		field.setRuntimeMetadata(FIELD_RESOLVER, factory);
	}
}

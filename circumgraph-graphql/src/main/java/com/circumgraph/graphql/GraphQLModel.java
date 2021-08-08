package com.circumgraph.graphql;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.storage.StorageModel;

/**
 * Utilities for accessing and setting things related to the generated GraphQL
 * API.
 */
public class GraphQLModel
{
	private static MetadataKey<FieldResolverFactory> FIELD_RESOLVER_FACTORY =
		MetadataKey.create("graphql:field-resolver-factory", FieldResolverFactory.class);

	/**
	 * Get if a custom {@link FieldResolverFactory} has been set for the given
	 * field.
	 *
	 * @param field
	 * @return
	 */
	public static Optional<FieldResolverFactory> getFieldResolverFactory(FieldDef field)
	{
		return field.getMetadata(FIELD_RESOLVER_FACTORY);
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
		field.setMetadata(FIELD_RESOLVER_FACTORY, factory);
	}
}

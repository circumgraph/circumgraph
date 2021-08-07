package com.circumgraph.graphql;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.MetadataKey;

/**
 * Utilities for accessing and setting things related to the generated GraphQL
 * API.
 */
public class GraphQLModel
{
	private static MetadataKey<FieldResolver> FIELD_RESOLVER = MetadataKey.create("graphql:field-resolver", FieldResolver.class);

	/**
	 * Get if a custom {@link FieldResolver} has been set for the given field.
	 *
	 * @param def
	 * @return
	 */
	public static Optional<FieldResolver> getFieldResolver(FieldDef def)
	{
		return def.getMetadata(FIELD_RESOLVER);
	}

	/**
	 * Set a custom {@link FieldResolver} to use for the given field.
	 *
	 * @param def
	 * @param resolver
	 */
	public static void setFieldResolver(FieldDef def, FieldResolver resolver)
	{
		def.setMetadata(FIELD_RESOLVER, resolver);
	}
}

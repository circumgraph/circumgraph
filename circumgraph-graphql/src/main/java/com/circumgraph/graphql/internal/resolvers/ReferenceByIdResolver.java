package com.circumgraph.graphql.internal.resolvers;

import com.circumgraph.graphql.FieldResolver;
import com.circumgraph.graphql.FieldResolverFactory;
import com.circumgraph.graphql.internal.StorageContext;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.Collection;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectRef;
import com.circumgraph.storage.StructuredValue;

import graphql.schema.DataFetchingEnvironment;

/**
 * {@link FieldResolver} that handles references to other objects.
 */
public class ReferenceByIdResolver
	implements FieldResolver
{
	private final Collection collection;
	private final String key;

	public ReferenceByIdResolver(Collection collection, String key)
	{
		this.collection = collection;
		this.key = key;
	}

	@Override
	public Object resolve(DataFetchingEnvironment env)
	{
		StructuredValue source = env.getSource();
		var value = source.getField(key, StoredObjectRef.class);
		if(value.isEmpty())
		{
			return null;
		}

		// Fetch the object in the current transaction
		StorageContext ctx = env.getContext();
		return ctx.getTx().wrap(collection.get(value.get().getId()));
	}

	/**
	 * Create a factory that will resolve an instance of
	 * {@link ReferenceByIdResolver}.
	 *
	 * @param def
	 *   the definition of the entity being linked to
	 * @param key
	 *   key where the id is found
	 * @return
	 *   factory
	 */
	public static FieldResolverFactory factory(StructuredDef def, String key)
	{
		var entity = def.findImplements(interfaceDef -> interfaceDef.hasImplements(StorageSchema.ENTITY_NAME))
			.get();

		return encounter -> new ReferenceByIdResolver(
			encounter.getStorage().get(entity.getName()),
			key
		);
	}
}

package com.circumgraph.storage;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;

import org.eclipse.collections.api.factory.Lists;

/**
 * Schema describing the built-in types that the storage system requires.
 */
public class StorageSchema
	implements Schema
{
	public static final StorageSchema INSTANCE = new StorageSchema();

	private StorageSchema()
	{
	}

	@Override
	public Iterable<? extends TypeDef> getTypes()
	{
		// TODO: The Entity needs a directive for auto-generating ids
		return Lists.immutable.of(
			InterfaceDef.create("Entity")
				.addField(FieldDef.create("id")
					.withType(ScalarDef.ID)
					.build()
				)
				.build()
		);
	}
}

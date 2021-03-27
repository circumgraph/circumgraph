package com.circumgraph.storage;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.DirectiveValidator;
import com.circumgraph.storage.internal.Indexing;
import com.circumgraph.storage.internal.model.IndexDirectiveValidator;
import com.circumgraph.storage.internal.model.SortableDirectiveValidator;

import org.eclipse.collections.api.factory.Lists;

/**
 * Schema describing the built-in types that the storage system requires.
 */
// TODO: Validation that types only implement Entity *once* either directly or via a single interface
public class StorageSchema
	implements Schema
{
	public static final String ENTITY_NAME = "Entity";

	public static final StorageSchema INSTANCE = new StorageSchema();

	private StorageSchema()
	{
	}

	@Override
	public Iterable<? extends DirectiveValidator<?>> getDirectiveValidators()
	{
		return Lists.immutable.of(
			new IndexDirectiveValidator(new Indexing()),
			new SortableDirectiveValidator()
		);
	}

	@Override
	public Iterable<? extends TypeDef> getTypes()
	{
		return Lists.immutable.of(
			InterfaceDef.create(ENTITY_NAME)
				.addField(FieldDef.create("id")
					.withType(ScalarDef.ID)
					.build()
				)
				.build()
		);
	}
}

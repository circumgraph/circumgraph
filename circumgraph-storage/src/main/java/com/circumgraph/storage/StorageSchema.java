package com.circumgraph.storage;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.TypeDefProcessor;
import com.circumgraph.storage.internal.ValueIndexers;
import com.circumgraph.storage.internal.ValueProviders;
import com.circumgraph.storage.internal.model.DefaultDirectiveProcessor;
import com.circumgraph.storage.internal.model.IndexDirectiveProcessor;
import com.circumgraph.storage.internal.model.ReadonlyDirectiveProcessor;
import com.circumgraph.storage.internal.model.SortableDirectiveProcessor;
import com.circumgraph.storage.internal.processors.InterfaceImplementationProcessor;

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
	public Iterable<? extends DirectiveUseProcessor<?>> getDirectiveUseProcessors()
	{
		return Lists.immutable.of(
			new DefaultDirectiveProcessor(new ValueProviders()),
			new ReadonlyDirectiveProcessor(),

			new IndexDirectiveProcessor(new ValueIndexers()),
			new SortableDirectiveProcessor()
		);
	}

	@Override
	public Iterable<? extends TypeDefProcessor<?>> getTypeDefProcessors()
	{
		return Lists.immutable.of(
			new InterfaceImplementationProcessor()
		);
	}

	@Override
	public Iterable<? extends TypeDef> getTypes()
	{
		return Lists.immutable.of(
			InterfaceDef.create(ENTITY_NAME)
				.addField(FieldDef.create("id")
					.withType(NonNullDef.output(ScalarDef.ID))
					.addDirective(DirectiveUse.create("default")
						.addArgument("provider", "ID")
						.build()
					)
					.addDirective(DirectiveUse.create("readonly")
						.build()
					)
					.addDirective(DirectiveUse.create("index")
						.build()
					)
					.build()
				)
				.build()
		);
	}
}

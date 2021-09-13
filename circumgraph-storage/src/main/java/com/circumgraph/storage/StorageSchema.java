package com.circumgraph.storage;

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
import com.circumgraph.storage.internal.indexing.AutoGeneratedIdValueIndexer;
import com.circumgraph.storage.internal.processors.AutoGeneratedDirectiveProcessor;
import com.circumgraph.storage.internal.processors.DefaultDirectiveProcessor;
import com.circumgraph.storage.internal.processors.IndexDirectiveProcessor;
import com.circumgraph.storage.internal.processors.InterfaceImplementationProcessor;
import com.circumgraph.storage.internal.processors.ReadonlyDirectiveProcessor;
import com.circumgraph.storage.internal.processors.SortableDirectiveProcessor;
import com.circumgraph.storage.internal.providers.GeneratedIdValueProvider;

import org.eclipse.collections.api.factory.Lists;

import se.l4.ylem.ids.SimpleLongIdGenerator;

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
		var valueProviders = new ValueProviders();
		return Lists.immutable.of(
			new DefaultDirectiveProcessor(valueProviders),
			new ReadonlyDirectiveProcessor(),
			new AutoGeneratedDirectiveProcessor(valueProviders),

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
					.withMetadata(StorageModel.FIELD_DEFAULT_VALUE_PROVIDER, new GeneratedIdValueProvider(new SimpleLongIdGenerator()))
					.withMetadata(StorageModel.FIELD_MUTATION, StorageModel.MutationType.NEVER)
					.withMetadata(StorageModel.FIELD_INDEXED, true)
					.withMetadata(StorageModel.FIELD_INDEXER, AutoGeneratedIdValueIndexer.INSTANCE)
					.build()
				)
				.build()
		);
	}
}

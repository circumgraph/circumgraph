package com.circumgraph.storage.internal;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.internal.mappers.ListValueMapper;
import com.circumgraph.storage.internal.mappers.PolymorphicValueMapper;
import com.circumgraph.storage.internal.mappers.RootObjectMapper;
import com.circumgraph.storage.internal.mappers.ScalarValueMapper;
import com.circumgraph.storage.internal.mappers.StoredObjectRefMapper;
import com.circumgraph.storage.internal.mappers.StructuredValueMapper;
import com.circumgraph.storage.internal.mappers.ValueMapper;
import com.circumgraph.storage.mutation.StructuredMutation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.SetIterable;

import se.l4.silo.StorageException;

/**
 * Utility class that creates {@link ValueMapper}s.
 */
public class ValueMappers
{
	private final Model model;
	private final Storage storage;

	public ValueMappers(
		Model model,
		Storage storage
	)
	{
		this.model = model;
		this.storage = storage;
	}

	public ValueMapper<StoredObjectValue, StructuredMutation> createRoot(
		StructuredDef def
	)
	{
		var polymorphic = createPolymorphic(def);
		return new RootObjectMapper(polymorphic);
	}

	/**
	 * Create a polymorphic mapper. This type of mapper either receives a
	 * single {@link ObjectDef} which is used directly or a {@link InterfaceDef}
	 * where all implementations will be used.
	 *
	 * @param def
	 * @return
	 */
	private PolymorphicValueMapper createPolymorphic(StructuredDef def)
	{
		SetIterable<ObjectDef> defs;
		if(def instanceof ObjectDef)
		{
			defs = Sets.immutable.of((ObjectDef) def);
		}
		else
		{
			defs = model
				.findImplements(def.getName())
				.selectInstancesOf(ObjectDef.class);
		}

		return new PolymorphicValueMapper(
			def,
			defs.toMap(TypeDef::getName, this::createDirect),
			Lists.immutable.empty()
		);
	}

	private ValueMapper<?, ?> create(TypeDef def)
	{
		if(def instanceof ListDef)
		{
			return new ListValueMapper<>(
				(ListDef) def,
				Lists.immutable.empty(),
				create(((ListDef) def).getItemType())
			);
		}
		else if(def instanceof StructuredDef)
		{
			var structuredDef = (StructuredDef) def;
 			if(structuredDef.findImplements(StorageSchema.ENTITY_NAME))
			{
				/*
				 * Links to other entities may also be polymorphic in that they
				 * may be declared as a more specific type of an entity. In
				 * that case we need to find the interface that directly
				 * implements `Entity` to set the correct definition on mapped
				 * values.
				 */
				if(! structuredDef.hasImplements(StorageSchema.ENTITY_NAME))
				{
					structuredDef = structuredDef.findImplements(interfaceDef -> interfaceDef.hasImplements(StorageSchema.ENTITY_NAME))
						.get();
				}

				var entityName = structuredDef.getName();
				return new StoredObjectRefMapper(structuredDef, () -> storage.get(entityName));
			}

			return createPolymorphic(structuredDef);
		}
		else if(def instanceof UnionDef)
		{

		}
		else if(def instanceof EnumDef)
		{

		}
		else if(def instanceof ScalarDef)
		{
			return new ScalarValueMapper(
				(ScalarDef) def,
				null,
				Lists.immutable.empty()
			);
		}

		throw new StorageException("Unable to create a mapper for " + def);
	}

	private StructuredValueMapper createDirect(StructuredDef def)
	{
		var isEntity = def.findImplements(StorageSchema.ENTITY_NAME);
		return new StructuredValueMapper(
			def,
			def.getFields()
				.select(f -> isStoredField(isEntity, f))
				.toMap(FieldDef::getName, f -> create(f.getType())),
			Lists.immutable.empty()
		);
	}

	private boolean isStoredField(boolean isEntity, FieldDef field)
	{
		if(isEntity && field.getName().equals("id"))
		{
			return false;
		}

		return field.getArguments().isEmpty();
	}
}

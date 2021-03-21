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
import com.circumgraph.storage.StoredEntityValue;
import com.circumgraph.storage.internal.mappers.EntityMapper;
import com.circumgraph.storage.internal.mappers.ListValueMapper;
import com.circumgraph.storage.internal.mappers.PolymorphicValueMapper;
import com.circumgraph.storage.internal.mappers.ScalarValueMapper;
import com.circumgraph.storage.internal.mappers.StructuredValueMapper;
import com.circumgraph.storage.internal.mappers.ValueMapper;
import com.circumgraph.storage.mutation.StructuredMutation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.SetIterable;

import se.l4.silo.StorageException;

public class EntityMappers
{
	private final Model model;

	public EntityMappers(
		Model model
	)
	{
		this.model = model;
	}

	public ValueMapper<StoredEntityValue, StructuredMutation> createRoot(
		StructuredDef def
	)
	{
		var polymorphic = createPolymorphic(def);
		return new EntityMapper(polymorphic);
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
			return createPolymorphic((StructuredDef) def);
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
		var isEntity = def.findImplements("Entity");
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

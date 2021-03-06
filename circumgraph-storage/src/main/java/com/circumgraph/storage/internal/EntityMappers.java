package com.circumgraph.storage.internal;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.StoredEntityValue;
import com.circumgraph.storage.internal.mappers.EntityMapper;
import com.circumgraph.storage.internal.mappers.PolymorphicValueMapper;
import com.circumgraph.storage.internal.mappers.ScalarValueMapper;
import com.circumgraph.storage.internal.mappers.StructuredValueMapper;
import com.circumgraph.storage.internal.mappers.ValueMapper;
import com.circumgraph.storage.mutation.StructuredMutation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

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

	private PolymorphicValueMapper createPolymorphic(StructuredDef def)
	{
		ImmutableList<ObjectDef> defs;
		if(def instanceof ObjectDef)
		{
			defs = Lists.immutable.of((ObjectDef) def);
		}
		else
		{
			// TODO: Implements all

			defs = model
				.getImplements(def.getName())
				.selectInstancesOf(ObjectDef.class)
				.toImmutable();
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
		return new StructuredValueMapper(
			def,
			def.getFields()
				.select(this::isStoredField)
				.toMap(FieldDef::getName, f -> create(f.getType())),
			Lists.immutable.empty()
		);
	}

	private boolean isStoredField(FieldDef field)
	{
		return field.getArguments().isEmpty();
	}
}

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
import com.circumgraph.storage.internal.serializers.BooleanValueSerializer;
import com.circumgraph.storage.internal.serializers.FloatValueSerializer;
import com.circumgraph.storage.internal.serializers.IdValueSerializer;
import com.circumgraph.storage.internal.serializers.IntValueSerializer;
import com.circumgraph.storage.internal.serializers.ListValueSerializer;
import com.circumgraph.storage.internal.serializers.PolymorphicValueSerializer;
import com.circumgraph.storage.internal.serializers.StringValueSerializer;
import com.circumgraph.storage.internal.serializers.StructuredValueSerializer;
import com.circumgraph.storage.types.ValueSerializer;
import com.circumgraph.values.SimpleValue;
import com.circumgraph.values.StructuredValue;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.set.SetIterable;

import se.l4.silo.StorageException;

/**
 * Helper that resolves instances of {@link ValueSerializer} for entities.
 */
public class EntitySerializers
{
	private final Model model;
	private final MapIterable<ScalarDef, ValueSerializer<SimpleValue>> scalars;

	public EntitySerializers(
		Model model
	)
	{
		this.model = model;

		scalars = Maps.immutable.<ScalarDef, ValueSerializer<SimpleValue>>empty()
			.newWithKeyValue(ScalarDef.BOOLEAN, new BooleanValueSerializer())
			.newWithKeyValue(ScalarDef.FLOAT, new FloatValueSerializer())
			.newWithKeyValue(ScalarDef.INT, new IntValueSerializer())
			.newWithKeyValue(ScalarDef.STRING, new StringValueSerializer())
			.newWithKeyValue(ScalarDef.ID, new IdValueSerializer());
	}

	private ValueSerializer<?> resolve(TypeDef def)
	{
		if(def instanceof ListDef)
		{
			ListDef listDef = (ListDef) def;
			return new ListValueSerializer<>(
				listDef,
				resolve(listDef.getItemType())
			);
		}
		else if(def instanceof StructuredDef)
		{
			StructuredDef structuredDef = (StructuredDef) def;
 			if(structuredDef.getImplementsNames().contains("Entity"))
			{
				throw new IllegalArgumentException(def + " is a reference to another entity");
			}

			return resolvePolymorphic(structuredDef);
		}
		else if(def instanceof UnionDef)
		{

		}
		else if(def instanceof EnumDef)
		{

		}
		else if(def instanceof ScalarDef)
		{
			ValueSerializer<?> serializer = scalars.get(def);
			if(serializer != null)
			{
				return serializer;
			}
		}

		throw new StorageException("Unable to create a serializer for " + def);
	}

	/**
	 * Create a polymorphic serializer. This type of serializer either receives a
	 * single {@link ObjectDef} which is used directly or a {@link InterfaceDef}
	 * where all implementations will be used.
	 *
	 * @param def
	 * @return
	 */
	public PolymorphicValueSerializer resolvePolymorphic(StructuredDef def)
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

		return new PolymorphicValueSerializer(
			def,
			defs.toMap(TypeDef::getName, this::resolveDirect)
		);
	}

	private ValueSerializer<StructuredValue> resolveDirect(StructuredDef def)
	{
		return new StructuredValueSerializer(
			def,
			def.getFields()
				.select(this::isStoredField)
				.toMap(FieldDef::getName, f -> resolve(f.getType()))
		);
	}

	private boolean isStoredField(FieldDef field)
	{
		return field.getArguments().isEmpty();
	}
}

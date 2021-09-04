package com.circumgraph.storage.internal;

import java.util.LinkedList;

import com.circumgraph.model.EnumDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.HasMetadata;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.Model;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.internal.serializers.BooleanValueSerializer;
import com.circumgraph.storage.internal.serializers.DeferredValueSerializer;
import com.circumgraph.storage.internal.serializers.DurationValueSerializer;
import com.circumgraph.storage.internal.serializers.EnumValueSerializer;
import com.circumgraph.storage.internal.serializers.FloatValueSerializer;
import com.circumgraph.storage.internal.serializers.IdValueSerializer;
import com.circumgraph.storage.internal.serializers.IntValueSerializer;
import com.circumgraph.storage.internal.serializers.ListValueSerializer;
import com.circumgraph.storage.internal.serializers.LocalDateTimeValueSerializer;
import com.circumgraph.storage.internal.serializers.LocalDateValueSerializer;
import com.circumgraph.storage.internal.serializers.LocalTimeValueSerializer;
import com.circumgraph.storage.internal.serializers.OffsetDateTimeValueSerializer;
import com.circumgraph.storage.internal.serializers.OffsetTimeValueSerializer;
import com.circumgraph.storage.internal.serializers.PolymorphicValueSerializer;
import com.circumgraph.storage.internal.serializers.StoredObjectRefSerializer;
import com.circumgraph.storage.internal.serializers.StringValueSerializer;
import com.circumgraph.storage.internal.serializers.StructuredValueSerializer;
import com.circumgraph.storage.internal.serializers.ZonedDateTimeValueSerializer;
import com.circumgraph.storage.types.ValueSerializer;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;

import se.l4.silo.StorageException;

/**
 * Helper that resolves instances of {@link ValueSerializer}.
 */
public class ValueSerializers
{
	@SuppressWarnings("rawtypes")
	public static final MetadataKey<ValueSerializer> SERIALIZER = MetadataKey.create("storage:serializer", ValueSerializer.class);

	private final Model model;
	private final MapIterable<ScalarDef, ValueSerializer<SimpleValue>> scalars;

	public ValueSerializers(
		Model model
	)
	{
		this.model = model;

		scalars = Maps.immutable.<ScalarDef, ValueSerializer<SimpleValue>>empty()
			.newWithKeyValue(ScalarDef.BOOLEAN, new BooleanValueSerializer())
			.newWithKeyValue(ScalarDef.FLOAT, new FloatValueSerializer())
			.newWithKeyValue(ScalarDef.INT, new IntValueSerializer())
			.newWithKeyValue(ScalarDef.STRING, new StringValueSerializer())
			.newWithKeyValue(ScalarDef.ID, new IdValueSerializer())
			.newWithKeyValue(ScalarDef.LOCAL_DATE, new LocalDateValueSerializer())
			.newWithKeyValue(ScalarDef.LOCAL_TIME, new LocalTimeValueSerializer())
			.newWithKeyValue(ScalarDef.LOCAL_DATE_TIME, new LocalDateTimeValueSerializer())
			.newWithKeyValue(ScalarDef.OFFSET_TIME, new OffsetTimeValueSerializer())
			.newWithKeyValue(ScalarDef.OFFSET_DATE_TIME, new OffsetDateTimeValueSerializer())
			.newWithKeyValue(ScalarDef.ZONED_DATE_TIME, new ZonedDateTimeValueSerializer())
			.newWithKeyValue(ScalarDef.DURATION, new DurationValueSerializer());
	}

	private ValueSerializer<?> resolve(OutputTypeDef def)
	{
		if(def instanceof HasMetadata md)
		{
			var current = md.getMetadata(SERIALIZER);
			if(current.isPresent())
			{
				return current.get();
			}

			md.setRuntimeMetadata(SERIALIZER, new DeferredValueSerializer(md));
		}

		ValueSerializer<?> serializer;
		if(def instanceof NonNullDef.Output nonNullDef)
		{
			serializer = resolve(nonNullDef.getType());
		}
		else if(def instanceof ListDef.Output listDef)
		{
			serializer = new ListValueSerializer<>(
				listDef,
				resolve(listDef.getItemType())
			);
		}
		else if(def instanceof StructuredDef)
		{
			serializer = resolvePolymorphic(
				def,
				Lists.immutable.of(def),
				true
			);
		}
		else if(def instanceof UnionDef unionDef)
		{
			serializer = resolvePolymorphic(
				def,
				unionDef.getTypes(),
				true
			);
		}
		else if(def instanceof EnumDef enumDef)
		{
			serializer = new EnumValueSerializer(enumDef);
		}
		else if(def instanceof ScalarDef)
		{
			serializer = scalars.get(def);
		}
		else
		{
			throw new StorageException("Unable to create a serializer for " + def);
		}

		if(def instanceof HasMetadata md)
		{
			md.setRuntimeMetadata(SERIALIZER, serializer);
		}

		return serializer;
	}

	/**
	 * Create a polymorphic serializer. This type of serializer either receives a
	 * single {@link ObjectDef} which is used directly or a {@link InterfaceDef}
	 * where all implementations will be used.
	 *
	 * @param def
	 * @return
	 */
	public PolymorphicValueSerializer resolvePolymorphic(
		OutputTypeDef def,
		RichIterable<? extends OutputTypeDef> initialDefs,
		boolean allowReferences
	)
	{
		MutableMap<String, ValueSerializer<?>> defs = Maps.mutable.empty();

		var queue = new LinkedList<OutputTypeDef>();
		initialDefs.each(queue::add);

		while(! queue.isEmpty())
		{
			var subDef = queue.poll();
			if(allowReferences
				&& subDef instanceof StructuredDef
				&& ((StructuredDef) subDef).findImplements(StorageSchema.ENTITY_NAME))
			{
				var structuredDef = (StructuredDef) subDef;

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
				defs.put(
					entityName,
					new StoredObjectRefSerializer(structuredDef)
				);
			}
			else if(subDef instanceof ObjectDef objectDef)
			{
				defs.put(subDef.getName(), resolveDirect(objectDef));
			}
			else if(subDef instanceof InterfaceDef interfaceDef)
			{
				interfaceDef.getImplementors().each(queue::add);
			}
		}

		return new PolymorphicValueSerializer(
			def,
			defs
		);
	}

	private ValueSerializer<StructuredValue> resolveDirect(StructuredDef def)
	{
		return new StructuredValueSerializer(
			def,
			def.getFields()
				.select(f -> StorageModel.getFieldType(f) == StorageModel.FieldType.STORED)
				.toMap(FieldDef::getName, f -> resolve(f.getType()))
		);
	}
}

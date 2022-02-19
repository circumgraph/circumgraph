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
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.internal.mappers.DeferredValueMapper;
import com.circumgraph.storage.internal.mappers.EnumValueMapper;
import com.circumgraph.storage.internal.mappers.ListValueMapper;
import com.circumgraph.storage.internal.mappers.PolymorphicValueMapper;
import com.circumgraph.storage.internal.mappers.ReadOnlyMapper;
import com.circumgraph.storage.internal.mappers.RootObjectMapper;
import com.circumgraph.storage.internal.mappers.ScalarValueMapper;
import com.circumgraph.storage.internal.mappers.StoredObjectRefMapper;
import com.circumgraph.storage.internal.mappers.StructuredValueMapper;
import com.circumgraph.storage.internal.providers.EmptyValueProvider;
import com.circumgraph.storage.internal.validators.NonNullValueValidator;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.types.ValueMapper;
import com.circumgraph.storage.types.ValueProvider;
import com.circumgraph.storage.types.ValueValidator;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;

import reactor.core.publisher.Flux;
import se.l4.silo.StorageException;

/**
 * Utility class that creates {@link ValueMapper}s.
 */
public class ValueMappers
{
	@SuppressWarnings("rawtypes")
	public static final MetadataKey<ValueMapper> MAPPER = MetadataKey.create("storage:mapper", ValueMapper.class);

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

	/**
	 * Create the root mapper for the given definition.
	 *
	 * @param def
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ValueMapper<StoredObjectValue, StructuredMutation> createRoot(
		StructuredDef def
	)
	{
		var polymorphic = createPolymorphic(
			def,
			Lists.immutable.of(def),
			false,
			false
		);
		return new RootObjectMapper((ValueMapper) polymorphic);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ValueMutationHandler<?, ?> createHandler(FieldDef field)
	{
		var mapper = resolveMapper(field.getType());

		var mutationType = StorageModel.getFieldMutation(field);
		switch(mutationType)
		{
			case CREATABLE:
				mapper = new ReadOnlyMapper(mapper, false);
				break;
			case NEVER:
				mapper = new ReadOnlyMapper(mapper, true);
				break;
			case UPDATEABLE:
				// Nothing needed
				break;
		}

		return new ValueMutationHandlerImpl(
			mapper,
			resolveProvider(field),
			resolveValidator(field),
			StorageModel.isRegenerateOnMutate(field)
		);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ValueMutationHandler<?, ?> createItemHandler(OutputTypeDef type)
	{
		var mapper = resolveMapper(type);

		return new ValueMutationHandlerImpl(
			mapper,
			new EmptyValueProvider(type),
			type instanceof NonNullDef ? NonNullValueValidator.instance() : ValueValidator.empty(),
			false
		);
	}

	@SuppressWarnings({ "rawtypes" })
	private ValueMapper<?, ?> resolveMapper(OutputTypeDef def)
	{
		if(def instanceof HasMetadata md)
		{
			var current = md.getMetadata(MAPPER);
			if(current.isPresent())
			{
				return current.get();
			}

			md.setRuntimeMetadata(MAPPER, new DeferredValueMapper(md));
		}

		ValueMapper mapper;
		if(def instanceof NonNullDef.Output nonNullDef)
		{
			mapper = resolveMapper(nonNullDef.getType());
		}
		else if(def instanceof ListDef.Output listDef)
		{
			mapper = new ListValueMapper<>(
				listDef,
				createItemHandler(listDef.getItemType())
			);
		}
		else if(def instanceof StructuredDef)
		{
			mapper = createPolymorphic(
				def,
				Lists.immutable.of(def),
				true,
				false
			);
		}
		else if(def instanceof UnionDef unionDef)
		{
			mapper = createPolymorphic(
				def,
				unionDef.getTypes(),
				true,
				true
			);
		}
		else if(def instanceof EnumDef enumDef)
		{
			mapper = new EnumValueMapper(enumDef);
		}
		else if(def instanceof ScalarDef scalarDef)
		{
			mapper = new ScalarValueMapper(scalarDef);
		}
		else
		{
			throw new StorageException("Unable to create a mapper for " + def);
		}

		if(def instanceof HasMetadata md)
		{
			md.setRuntimeMetadata(MAPPER, mapper);
		}

		return mapper;
	}

	/**
	 * Create a polymorphic mapper. This type of mapper either receives a
	 * single {@link ObjectDef} which is used directly or a {@link InterfaceDef}
	 * where all implementations will be used.
	 *
	 * @param def
	 * @return
	 */
	private PolymorphicValueMapper createPolymorphic(
		OutputTypeDef def,
		RichIterable<? extends OutputTypeDef> initialDefs,
		boolean allowReferences,
		boolean diverging
	)
	{
		MutableMap<String, ValueMapper<?, ?>> defs = Maps.mutable.empty();

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

					// TODO: Apply some validation so that only the specific type can be saved
				}

				var entityName = structuredDef.getName();
				defs.put(
					entityName,
					new StoredObjectRefMapper(structuredDef, () -> storage.get(entityName))
				);
			}
			else if(subDef instanceof ObjectDef)
			{
				defs.put(subDef.getName(), createDirect((ObjectDef) subDef));
			}
			else if(subDef instanceof InterfaceDef i)
			{
				i.getImplementors().each(queue::add);
			}
		}

		return new PolymorphicValueMapper(
			def,
			defs,
			diverging
		);
	}

	private StructuredValueMapper createDirect(StructuredDef def)
	{
		return new StructuredValueMapper(
			def,
			def.getFields()
				.select(f -> StorageModel.getFieldType(f) == StorageModel.FieldType.STORED)
				.toMap(FieldDef::getName, this::createHandler)
		);
	}

	@SuppressWarnings("unchecked")
	private <V extends Value> ValueValidator<V> resolveValidator(
		FieldDef def
	)
	{
		MutableList<ValueValidator<V>> result = Lists.mutable.empty();
		if(def.getType() instanceof NonNullDef)
		{
			result.add(NonNullValueValidator.instance());
		}

		if(result.isEmpty())
		{
			return ValueValidator.empty();
		}
		else if(result.size() == 1)
		{
			return result.get(0);
		}
		else
		{
			var flux = Flux.fromArray(result.toArray(ValueValidator[]::new));
			return (loc, v) -> flux.flatMap(validator -> validator.validate(loc, v));
		}
	}

	private <V extends Value> ValueProvider resolveProvider(
		FieldDef field
	)
	{
		var provider = StorageModel.getDefaultProvider(field);
		if(provider.isPresent())
		{
			return provider.get();
		}

		return new EmptyValueProvider(field.getType());
	}

	private static class ValueMutationHandlerImpl<V extends Value, M extends Mutation>
		implements ValueMutationHandler<V, M>
	{
		private final OutputTypeDef def;
		private final ValueProvider defaultProvider;
		private final ValueMapper<V, M> mapper;
		private final ValueValidator<V> validator;
		private final boolean regenerate;

		public ValueMutationHandlerImpl(
			ValueMapper<V, M> mapper,
			ValueProvider defaultProvider,
			ValueValidator<V> validator,
			boolean regenerate
		)
		{
			this.def = mapper.getDef();
			this.mapper = mapper;
			this.defaultProvider = defaultProvider;
			this.validator = validator;
			this.regenerate = regenerate;
		}

		@Override
		public OutputTypeDef getDef()
		{
			return def;
		}

		@Override
		public ValueProvider getDefault()
		{
			return defaultProvider;
		}

		@Override
		public boolean isRegenerate()
		{
			return regenerate;
		}

		@Override
		public ValueMapper<V, M> getMapper()
		{
			return mapper;
		}

		@Override
		public ValueValidator<V> getValidator()
		{
			return validator;
		}
	}
}

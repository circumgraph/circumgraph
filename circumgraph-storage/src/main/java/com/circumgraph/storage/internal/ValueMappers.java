package com.circumgraph.storage.internal;

import java.util.LinkedList;

import com.circumgraph.model.ArgumentUse;
import com.circumgraph.model.EnumDef;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.HasDirectives;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.Model;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.UnionDef;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StorageModel;
import com.circumgraph.storage.StorageSchema;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.internal.mappers.EnumValueMapper;
import com.circumgraph.storage.internal.mappers.ListValueMapper;
import com.circumgraph.storage.internal.mappers.PolymorphicValueMapper;
import com.circumgraph.storage.internal.mappers.ReadOnlyMapper;
import com.circumgraph.storage.internal.mappers.RootObjectMapper;
import com.circumgraph.storage.internal.mappers.ScalarValueMapper;
import com.circumgraph.storage.internal.mappers.StoredObjectRefMapper;
import com.circumgraph.storage.internal.mappers.StructuredValueMapper;
import com.circumgraph.storage.internal.mappers.ValueMapper;
import com.circumgraph.storage.internal.validators.NonNullValueValidator;
import com.circumgraph.storage.mutation.StructuredMutation;
import com.circumgraph.storage.types.ValueProvider;
import com.circumgraph.storage.types.ValueValidator;
import com.circumgraph.values.StructuredValue;
import com.circumgraph.values.Value;

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
	private static final ValueValidator<?> EMPTY_VALIDATOR = (loc, v) -> Flux.empty();

	private final Model model;
	private final Storage storage;

	private final ValueProviders providers;

	public ValueMappers(
		Model model,
		Storage storage,
		ValueProviders providers
	)
	{
		this.model = model;
		this.storage = storage;
		this.providers = providers;
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
			(ValueValidator<Value>) EMPTY_VALIDATOR,
			false
		);
		return new RootObjectMapper((ValueMapper) polymorphic);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ValueMapper<?, ?> create(OutputTypeDef def, HasDirectives ctx)
	{
		var mapper = create0(def, ctx);
		if(ctx != null && ctx.getDirective("readonly").isPresent())
		{
			return new ReadOnlyMapper(mapper);
		}

		return mapper;
	}

	private ValueMapper<?, ?> create0(OutputTypeDef def, HasDirectives ctx)
	{
		boolean nonNull = false;
		if(def instanceof NonNullDef.Output)
		{
			// Resolve NonNullDef - non-null is applied as validation
			nonNull = true;
			def = ((NonNullDef.Output) def).getType();
		}

		if(def instanceof ListDef.Output)
		{
			var listDef = (ListDef.Output) def;
			return new ListValueMapper<>(
				listDef,
				resolveValidator(ctx, nonNull),
				create(listDef.getItemType(), null)
			);
		}
		else if(def instanceof StructuredDef)
		{
			return createPolymorphic(
				def,
				Lists.immutable.of(def),
				resolveValidator(ctx, nonNull),
				true
			);
		}
		else if(def instanceof UnionDef)
		{
			return createPolymorphic(
				def,
				((UnionDef) def).getTypes(),
				resolveValidator(ctx, nonNull),
				true
			);
		}
		else if(def instanceof EnumDef)
		{
			return new EnumValueMapper(
				(EnumDef) def,
				resolveProvider((EnumDef) def, ctx),
				resolveValidator(ctx, nonNull)
			);
		}
		else if(def instanceof ScalarDef)
		{
			return new ScalarValueMapper(
				(ScalarDef) def,
				resolveProvider((ScalarDef) def, ctx),
				resolveValidator(ctx, nonNull)
			);
		}

		throw new StorageException("Unable to create a mapper for " + def);
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
		ValueValidator<Value> validator,
		boolean allowReferences
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
			else if(subDef instanceof InterfaceDef)
			{
				model.getImplements(subDef.getName())
					.each(queue::add);
			}
		}

		return new PolymorphicValueMapper(
			def,
			defs,
			validator
		);
	}

	private StructuredValueMapper createDirect(StructuredDef def)
	{
		return new StructuredValueMapper(
			def,
			def.getFields()
				.select(f -> StorageModel.getFieldType(f) == StorageModel.FieldType.STORED)
				.toMap(FieldDef::getName, f -> create(f.getType(), f)),
			(ValueValidator<StructuredValue>) EMPTY_VALIDATOR
		);
	}

	@SuppressWarnings("unchecked")
	private <V extends Value> ValueValidator<V> resolveValidator(
		HasDirectives ctx,
		boolean nonNull
	)
	{
		MutableList<ValueValidator<V>> result = Lists.mutable.empty();
		if(nonNull)
		{
			result.add(NonNullValueValidator.instance());
		}

		if(result.isEmpty())
		{
			return (ValueValidator<V>) EMPTY_VALIDATOR;
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

	@SuppressWarnings("unchecked")
	private <V extends Value> ValueProvider<V> resolveProvider(
		SimpleValueDef def,
		HasDirectives ctx
	)
	{
		if(ctx == null) return null;

		var defaultDirective = ctx.getDirective("default");
		if(defaultDirective.isEmpty()) return null;

		var directive = defaultDirective.get();

		var providerArg = directive.getArgument("provider")
			.flatMap(ArgumentUse::getValueAsString);

		if(providerArg.isPresent())
		{
			// If there's a provider - return it
			return (ValueProvider<V>) providers.get(providerArg.get()).get();
		}

		return null;
	}
}

package com.circumgraph.model.internal;

import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;
import com.circumgraph.model.processing.TypeDefProcessor;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

/**
 * Basic implementation of {@link Schema}.
 */
public class SchemaImpl
	implements Schema
{
	private final ImmutableSet<? extends TypeDef> types;
	private final ImmutableList<? extends DirectiveUseProcessor<?>> directiveUseProcessors;
	private final ImmutableList<? extends TypeDefProcessor<?>> typeDefProcessors;

	private SchemaImpl(
		ImmutableSet<? extends TypeDef> types,
		ImmutableList<? extends DirectiveUseProcessor<?>> directiveUseProcessors,
		ImmutableList<? extends TypeDefProcessor<?>> typeDefProcessors
	)
	{
		this.types = types;
		this.directiveUseProcessors = directiveUseProcessors;
		this.typeDefProcessors = typeDefProcessors;
	}

	@Override
	public Iterable<? extends TypeDef> getTypes()
	{
		return types;
	}

	@Override
	public Iterable<? extends DirectiveUseProcessor<?>> getDirectiveUseProcessors()
	{
		return directiveUseProcessors;
	}

	@Override
	public Iterable<? extends TypeDefProcessor<?>> getTypeDefProcessors()
	{
		return typeDefProcessors;
	}

	public static Builder create()
	{
		return new BuilderImpl(
			Sets.immutable.empty(),
			Lists.immutable.empty(),
			Lists.immutable.empty()
		);
	}

	private static class BuilderImpl
		implements Builder
	{
		private final ImmutableSet<TypeDef> types;
		private final ImmutableList<DirectiveUseProcessor<?>> directiveUseProcessors;
		private final ImmutableList<TypeDefProcessor<?>> typeDefProcessors;

		public BuilderImpl(
			ImmutableSet<TypeDef> types,

			ImmutableList<DirectiveUseProcessor<?>> directiveUseProcessors,
			ImmutableList<TypeDefProcessor<?>> typeDefProcessors
		)
		{
			this.types = types;
			this.directiveUseProcessors = directiveUseProcessors;
			this.typeDefProcessors = typeDefProcessors;
		}

		@Override
		public Builder addDirectiveUseProcessor(
			DirectiveUseProcessor<?> processor
		)
		{
			return new BuilderImpl(
				types,
				directiveUseProcessors.newWith(processor),
				typeDefProcessors
			);
		}

		@Override
		public Builder addDirectiveUseProcessors(
			Iterable<? extends DirectiveUseProcessor<?>> processors
		)
		{
			return new BuilderImpl(
				types,
				directiveUseProcessors.newWithAll(processors),
				typeDefProcessors
			);
		}

		@Override
		public Builder addTypeDefProcessor(TypeDefProcessor<?> processor)
		{
			return new BuilderImpl(
				types,
				directiveUseProcessors,
				typeDefProcessors.newWith(processor)
			);
		}

		@Override
		public Builder addTypeDefProcessors(
			Iterable<? extends TypeDefProcessor<?>> processors
		)
		{
			return new BuilderImpl(
				types,
				directiveUseProcessors,
				typeDefProcessors.newWithAll(processors)
			);
		}

		@Override
		public Builder addType(TypeDef type)
		{
			return new BuilderImpl(
				types.newWith(type),
				directiveUseProcessors,
				typeDefProcessors
			);
		}

		@Override
		public Builder addTypes(Iterable<? extends TypeDef> types)
		{
			return new BuilderImpl(
				this.types.newWithAll(types),
				directiveUseProcessors,
				typeDefProcessors
			);
		}

		@Override
		public Schema build()
		{
			return new SchemaImpl(
				types,
				directiveUseProcessors,
				typeDefProcessors
			);
		}
	}
}

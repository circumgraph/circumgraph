package com.circumgraph.model.internal;

import com.circumgraph.model.Schema;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.processing.DirectiveUseProcessor;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

/**
 * Basic implementation of {@link Schema}.
 */
public class SchemaImpl
	implements Schema
{
	private final ImmutableSet<? extends TypeDef> types;
	private final ImmutableSet<? extends DirectiveUseProcessor<?>> directiveUseProcessors;

	private SchemaImpl(
		ImmutableSet<? extends TypeDef> types,
		ImmutableSet<? extends DirectiveUseProcessor<?>> directiveUseProcessors
	)
	{
		this.types = types;
		this.directiveUseProcessors = directiveUseProcessors;
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

	public static Builder create()
	{
		return new BuilderImpl(
			Sets.immutable.empty(),
			Sets.immutable.empty()
		);
	}

	private static class BuilderImpl
		implements Builder
	{
		private final ImmutableSet<TypeDef> types;
		private final ImmutableSet<DirectiveUseProcessor<?>> directiveUseProcessors;

		public BuilderImpl(
			ImmutableSet<TypeDef> types,

			ImmutableSet<DirectiveUseProcessor<?>> directiveUseProcessors
		)
		{
			this.types = types;
			this.directiveUseProcessors = directiveUseProcessors;
		}

		@Override
		public Builder addDirectiveUseProcessor(
			DirectiveUseProcessor<?> processor
		)
		{
			return new BuilderImpl(
				types,
				directiveUseProcessors.newWith(processor)
			);
		}

		@Override
		public Builder addDirectiveUseProcessors(
			Iterable<? extends DirectiveUseProcessor<?>> processors
		)
		{
			return new BuilderImpl(
				types,
				directiveUseProcessors.newWithAll(processors)
			);
		}

		@Override
		public Builder addType(TypeDef type)
		{
			return new BuilderImpl(
				types.newWith(type),
				directiveUseProcessors
			);
		}

		@Override
		public Builder addTypes(Iterable<? extends TypeDef> types)
		{
			return new BuilderImpl(
				this.types.newWithAll(types),
				directiveUseProcessors
			);
		}

		@Override
		public Schema build()
		{
			return new SchemaImpl(
				types,
				directiveUseProcessors
			);
		}
	}
}

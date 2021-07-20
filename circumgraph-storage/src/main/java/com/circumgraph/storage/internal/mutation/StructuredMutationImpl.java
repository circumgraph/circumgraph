package com.circumgraph.storage.internal.mutation;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.mutation.StructuredMutation;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;

public class StructuredMutationImpl
	implements StructuredMutation
{
	private final StructuredDef type;
	private final ImmutableMap<String, Mutation> fields;

	public StructuredMutationImpl(
		StructuredDef type,
		ImmutableMap<String, Mutation> fields
	)
	{
		this.type = type;
		this.fields = fields;
	}

	@Override
	public StructuredDef getDef()
	{
		return type;
	}

	@Override
	public MapIterable<String, Mutation> getFields()
	{
		return fields;
	}

	public static Builder create(StructuredDef type)
	{
		return new BuilderImpl(type);
	}

	private static class BuilderImpl
		implements Builder
	{
		private final StructuredDef type;
		private final MutableMap<String, Mutation> mutations;

		public BuilderImpl(StructuredDef type)
		{
			this.type = type;
			mutations = Maps.mutable.empty();
		}

		@Override
		public Builder updateField(String field, Mutation mutation)
		{
			mutations.put(field, mutation);
			return this;
		}

		@Override
		public StructuredMutation build()
		{
			return new StructuredMutationImpl(type, mutations.toImmutable());
		}
	}
}

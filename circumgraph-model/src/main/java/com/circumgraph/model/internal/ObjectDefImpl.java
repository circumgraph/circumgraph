package com.circumgraph.model.internal;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.ObjectDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class ObjectDefImpl
	extends StructuredDefImpl
	implements ObjectDef
{
	public ObjectDefImpl(
		SourceLocation sourceLocation,
		String id,
		String description,
		ImmutableList<TypeDef> implementsTypes,
		ImmutableList<DirectiveUse> directives,
		ImmutableList<FieldDef> fields
	)
	{
		super(
			sourceLocation,
			id,
			description,
			implementsTypes,
			directives,
			fields
		);
	}

	@Override
	public String toString()
	{
		return "ObjectDef{name=" + name + "}";
	}

	public static BuilderImpl create(String name)
	{
		return new BuilderImpl(
			null,
			name,
			null,
			Lists.immutable.empty(),
			Lists.immutable.empty(),
			Lists.immutable.empty()
		);
	}

	public static class BuilderImpl
		extends StructuredDefImpl.AbstractBuilder<ObjectDef.Builder>
		implements ObjectDef.Builder
	{
		public BuilderImpl(
			SourceLocation sourceLocation,
			String id,
			String description,
			ImmutableList<FieldDef> fields,
			ImmutableList<TypeDef> implementsTypes,
			ImmutableList<DirectiveUse> directives
		)
		{
			super(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				directives
			);
		}

		@Override
		protected ObjectDef.Builder create(
			SourceLocation sourceLocation,
			String id,
			String description,
			ImmutableList<FieldDef> fields,
			ImmutableList<TypeDef> implementsTypes,
			ImmutableList<DirectiveUse> directives
		)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				directives
			);
		}

		@Override
		public ObjectDef build()
		{
			return new ObjectDefImpl(
				SourceLocation.automatic(sourceLocation),
				id,
				description,
				implementsTypes,
				directives,
				fields
			);
		}
	}
}

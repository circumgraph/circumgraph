package com.circumgraph.model.internal;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;
import com.circumgraph.model.validation.SourceLocation;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.SetIterable;

public class InterfaceDefImpl
	extends StructuredDefImpl
	implements InterfaceDef
{
	public InterfaceDefImpl(
		SourceLocation sourceLocation,
		String name,
		String description,
		ImmutableList<TypeDef> implementsTypes,
		ImmutableList<DirectiveUse> directives,
		ImmutableList<FieldDef> fields
	)
	{
		super(
			sourceLocation,
			name,
			description,
			implementsTypes,
			directives,
			fields
		);
	}

	@Override
	public SetIterable<? extends StructuredDef> getImplementors()
	{
		return defs.getAll().asLazy()
			.selectInstancesOf(StructuredDef.class)
			.select(type -> type.getImplementsNames().contains(this.name))
			.toSet();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SetIterable<? extends StructuredDef> getAllImplementors()
	{
		var result = Sets.mutable.<StructuredDef>empty();
		for(var def : getImplementors())
		{
			result.add(def);

			if(def instanceof InterfaceDef i)
			{
				result.withAll((SetIterable) i.getAllImplementors());
			}
		}

		return result;
	}

	@Override
	public boolean isAssignableFrom(TypeDef other)
	{
		if(this == other)
		{
			return true;
		}

		if(other instanceof StructuredDef)
		{
			for(var i : ((StructuredDef) other).getAllImplements())
			{
				if(i == this)
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public String toString()
	{
		return "InterfaceDef{name=" + name + "}";
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
		extends StructuredDefImpl.AbstractBuilder<InterfaceDef.Builder>
		implements InterfaceDef.Builder
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
		protected InterfaceDef.Builder create(
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
		public InterfaceDef build()
		{
			return new InterfaceDefImpl(
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

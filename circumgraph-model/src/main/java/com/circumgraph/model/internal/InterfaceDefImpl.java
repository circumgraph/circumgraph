package com.circumgraph.model.internal;

import com.circumgraph.model.DirectiveUse;
import com.circumgraph.model.FieldDef;
import com.circumgraph.model.InterfaceDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.model.TypeDef;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.SetIterable;

/**
 * Implementation of {@link InterfaceDef}.
 */
public class InterfaceDefImpl
	extends StructuredDefImpl
	implements InterfaceDef
{
	public InterfaceDefImpl(
		Location sourceLocation,
		String name,
		String description,
		ImmutableList<TypeDef> implementsTypes,
		ImmutableList<DirectiveUse> directives,
		ImmutableList<FieldDef> fields,
		Metadata metadata
	)
	{
		super(
			sourceLocation,
			name,
			description,
			implementsTypes,
			directives,
			fields,
			metadata
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
	public BuilderImpl derive()
	{
		return new BuilderImpl(
			sourceLocation,
			name,
			description,
			directFields,
			implementsTypes,
			directives,
			metadata.derive()
		);
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
			Lists.immutable.empty(),
			Metadata.empty()
		);
	}

	public static class BuilderImpl
		extends StructuredDefImpl.AbstractBuilder<InterfaceDef.Builder>
		implements InterfaceDef.Builder
	{
		public BuilderImpl(
			Location sourceLocation,
			String id,
			String description,
			ImmutableList<FieldDef> fields,
			ImmutableList<TypeDef> implementsTypes,
			ImmutableList<DirectiveUse> directives,
			Metadata metadata
		)
		{
			super(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				directives,
				metadata
			);
		}

		@Override
		protected InterfaceDef.Builder create(
			Location sourceLocation,
			String id,
			String description,
			ImmutableList<FieldDef> fields,
			ImmutableList<TypeDef> implementsTypes,
			ImmutableList<DirectiveUse> directives,
			Metadata metadata
		)
		{
			return new BuilderImpl(
				sourceLocation,
				id,
				description,
				fields,
				implementsTypes,
				directives,
				metadata
			);
		}

		@Override
		public InterfaceDef build()
		{
			return new InterfaceDefImpl(
				Location.automatic(sourceLocation),
				id,
				description,
				implementsTypes,
				directives,
				fields,
				metadata
			);
		}
	}
}

package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.ListDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeDef;

/**
 * Implementation of {@link ListDef}.
 */
public abstract class ListDefImpl
	implements ListDef, HasPreparation
{
	protected final TypeDef itemType;

	protected ModelDefs defs;

	protected ListDefImpl(TypeDef itemType)
	{
		this.itemType = itemType;
	}

	@Override
	public String getName()
	{
		return "[" + getItemType().getName() + "]";
	}

	@Override
	public Optional<String> getDescription()
	{
		return Optional.empty();
	}

	@Override
	public void prepare(ModelDefs defs)
	{
		this.defs = defs;
	}

	@Override
	public boolean isReady()
	{
		return defs != null;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(itemType);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ListDefImpl other = (ListDefImpl) obj;
		return Objects.equals(itemType, other.itemType);
	}

	@Override
	public String toString()
	{
		return "ListDef{" + getItemType() + "}";
	}

	public static class OutputImpl
		extends ListDefImpl
		implements Output
	{
		public OutputImpl(OutputTypeDef itemType)
		{
			super(itemType);
		}

		@Override
		public OutputTypeDef getItemType()
		{
			return defs.getType(itemType, OutputTypeDef.class);
		}
	}

	public static class InputImpl
		extends ListDefImpl
		implements Input
	{
		public InputImpl(InputTypeDef itemType)
		{
			super(itemType);
		}

		@Override
		public InputTypeDef getItemType()
		{
			return defs.getType(itemType, InputTypeDef.class);
		}
	}
}

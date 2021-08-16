package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.InputTypeDef;
import com.circumgraph.model.NonNullDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.TypeDef;

public abstract class NonNullDefImpl
	implements NonNullDef, HasPreparation
{
	protected final TypeDef type;
	protected ModelDefs defs;

	protected NonNullDefImpl(TypeDef type)
	{
		this.type = type;
	}

	@Override
	public String getName()
	{
		return type.getName() + '!';
	}

	@Override
	public Optional<String> getDescription()
	{
		return Optional.empty();
	}

	@Override
	public String getTypeName()
	{
		return type.getName();
	}

	@Override
	public void prepare(ModelDefs defs)
	{
		this.defs = defs;

		HasPreparation.maybePrepare(getType(), defs);
	}

	@Override
	public boolean isReady()
	{
		return defs != null;
	}

	@Override
	public boolean isAssignableFrom(TypeDef other)
	{
		if(other instanceof NonNullDef nonNull)
		{
			if(getClass() != other.getClass())
			{
				// Check that both NonNull instances are input/input or output/output
				return false;
			}

			return this.getType().isAssignableFrom(nonNull.getType());
		}

		return this.getType().isAssignableFrom(other);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(type);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		NonNullDefImpl other = (NonNullDefImpl) obj;
		return Objects.equals(type, other.type);
	}

	@Override
	public String toString()
	{
		return "NonNullDef{" + type + "}";
	}

	public static class OutputImpl
		extends NonNullDefImpl
		implements Output
	{
		public OutputImpl(OutputTypeDef type)
		{
			super(type);
		}

		@Override
		public OutputTypeDef getType()
		{
			return defs == null ? (OutputTypeDef) type : defs.getType(type, OutputTypeDef.class);
		}

		@Override
		public String toString()
		{
			return "NonNullDef.Output{" + type + "}";
		}
	}

	public static class InputImpl
		extends NonNullDefImpl
		implements Input
	{
		public InputImpl(InputTypeDef type)
		{
			super(type);
		}

		@Override
		public InputTypeDef getType()
		{
			return defs == null ? (InputTypeDef) type : defs.getType(type, InputTypeDef.class);
		}

		@Override
		public String toString()
		{
			return "NonNullDef.Input{" + type + "}";
		}
	}
}

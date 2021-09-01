package com.circumgraph.storage.internal;

import java.util.Objects;

import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.SimpleValue;

/**
 * Implementation of {@link SimpleValue}.
 */
public class SimpleValueImpl
	implements SimpleValue
{
	private final SimpleValueDef definition;
	private final Object object;

	public SimpleValueImpl(
		SimpleValueDef definition,
		Object object
	)
	{
		this.definition = definition;
		this.object = object;
	}

	@Override
	public SimpleValueDef getDefinition()
	{
		return definition;
	}

	@Override
	public Object get()
	{
		return object;
	}

	@Override
	public String asString()
	{
		return (String) object;
	}

	@Override
	public int asInt()
	{
		return (int) object;
	}

	@Override
	public double asFloat()
	{
		return (double) object;
	}

	@Override
	public boolean asBoolean()
	{
		return (boolean) object;
	}

	@Override
	public long asID()
	{
		return (long) object;
	}

	@Override
	public <T> T as(Class<T> type)
	{
		return type.cast(object);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(object);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		SimpleValueImpl other = (SimpleValueImpl) obj;
		return Objects.equals(object, other.object);
	}

	@Override
	public String toString()
	{
		return "SimpleValue{object=" + object + ", definition=" + definition + "}";
	}
}

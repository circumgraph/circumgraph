package com.circumgraph.values.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.values.StructuredValue;
import com.circumgraph.values.Value;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;

public class StructuredValueImpl
	implements StructuredValue
{
	private final StructuredDef definition;
	private final MapIterable<String, ? extends Value> fields;

	public StructuredValueImpl(
		StructuredDef definition,
		MapIterable<String, ? extends Value> fields
	)
	{
		this.definition = definition;
		this.fields = fields.toImmutable();
	}

	@Override
	public StructuredDef getDefinition()
	{
		return definition;
	}

	@Override
	public MapIterable<String, ? extends Value> getFields()
	{
		return fields;
	}

	@Override
	public Optional<? extends Value> getField(String name)
	{
		return Optional.ofNullable(fields.get(name));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V extends Value> Optional<? extends V> getField(
		String name,
		Class<V> type
	)
	{
		var value = fields.get(name);
		return type.isInstance(value) ? Optional.of((V) value) : Optional.empty();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(definition, fields);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		StructuredValueImpl other = (StructuredValueImpl) obj;
		return Objects.equals(definition, other.definition)
			&& Objects.equals(fields, other.fields);
	}

	@Override
	public String toString()
	{
		return "StructuredValueImpl{fields=" + fields + ", definition=" + definition + "}";
	}

	public static class BuilderImpl
		implements Builder
	{
		private final StructuredDef definition;
		private final MutableMap<String, Value> fields;

		public BuilderImpl(
			StructuredDef definition
		)
		{
			this.definition = definition;
			this.fields = Maps.mutable.empty();
		}

		@Override
		public Builder add(String name, Value value)
		{
			// TODO: Validate against definition
			fields.put(name, value);
			return this;
		}

		@Override
		public StructuredValue build()
		{
			return new StructuredValueImpl(definition, fields);
		}
	}
}

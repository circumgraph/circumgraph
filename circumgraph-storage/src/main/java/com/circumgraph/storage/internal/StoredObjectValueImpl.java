package com.circumgraph.storage.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.SimpleValue;
import com.circumgraph.storage.StoredObjectValue;
import com.circumgraph.storage.StructuredValue;
import com.circumgraph.storage.Value;

import org.eclipse.collections.api.map.MapIterable;

public class StoredObjectValueImpl
	implements StoredObjectValue
{
	private final long id;

	private StructuredValue value;
	private Supplier<StructuredValue> supplier;

	public StoredObjectValueImpl(
		long id,
		Supplier<StructuredValue> supplier
	)
	{
		this.id = id;
		this.supplier = supplier;
	}

	public StoredObjectValueImpl(
		StructuredValue value
	)
	{
		this.value = Objects.requireNonNull(value);
		this.id = value.getField("id", SimpleValue.class).get().asID();
	}

	@Override
	public long getId()
	{
		return id;
	}

	private StructuredValue get()
	{
		if(value == null)
		{
			value = supplier.get();
		}

		return value;
	}

	@Override
	public StructuredDef getDefinition()
	{
		return get().getDefinition();
	}

	@Override
	public MapIterable<String, ? extends Value> getFields()
	{
		return get().getFields();
	}

	@Override
	public Optional<? extends Value> getField(String name)
	{
		return get().getField(name);
	}

	@Override
	public <V extends Value> Optional<? extends V> getField(String name, Class<V> type)
	{
		return get().getField(name, type);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, supplier, value);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		StoredObjectValueImpl other = (StoredObjectValueImpl) obj;
		return id == other.id && Objects.equals(supplier, other.supplier) && Objects.equals(value, other.value);
	}

	@Override
	public String toString()
	{
		return "StoredEntityValue{id=" + id + "}";
	}
}

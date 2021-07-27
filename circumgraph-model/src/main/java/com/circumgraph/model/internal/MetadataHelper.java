package com.circumgraph.model.internal;

import java.util.Optional;

import com.circumgraph.model.HasMetadata;
import com.circumgraph.model.MetadataKey;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

/**
 * Helper for {@link HasMetadata}.
 */
public class MetadataHelper
	implements HasMetadata
{
	private final MutableMap<String, Object> values;

	public MetadataHelper()
	{
		values = Maps.mutable.empty();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <V> Optional<V> getMetadata(MetadataKey<V> key)
	{
		return (Optional) Optional.ofNullable(values.get(key.getId()));
	}

	@Override
	public <V> void setMetadata(MetadataKey<V> key, V value)
	{
		values.put(key.getId(), value);
	}
}

package com.circumgraph.model;

import com.circumgraph.model.internal.MetadataDefImpl;

/**
 * Definition of a metadata value.
 */
public interface MetadataDef
{
	/**
	 * Get the key.
	 *
	 * @return
	 */
	String getKey();

	/**
	 * Get the value.
	 *
	 * @return
	 */
	Object getValue();

	/**
	 * Create an instance for the given key.
	 *
	 * @param <V>
	 *   type of value
	 * @param key
	 *   key
	 * @param value
	 *   value
	 * @return
	 *   instance
	 */
	static <V> MetadataDef create(MetadataKey<V> key, V value)
	{
		return new MetadataDefImpl(key.getId(), value);
	}
}

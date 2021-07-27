package com.circumgraph.model;

import java.util.Optional;

/**
 * Indicate that something can contain metadata.
 */
public interface HasMetadata
{
	/**
	 * Get metadata from this type.
	 *
	 * @param key
	 *   key to get
	 * @return
	 *   optional containing the value if present, empty optional otherwise
	 */
	<V> Optional<V> getMetadata(MetadataKey<V> key);

	/**
	 * Set some metadata on this type.
	 *
	 * @param key
	 *   key to set
	 * @param value
	 *   value to set
	 */
	<V> void setMetadata(MetadataKey<V> key, V value);
}

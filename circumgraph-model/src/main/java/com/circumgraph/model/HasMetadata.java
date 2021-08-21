package com.circumgraph.model;

import java.util.Optional;

import org.eclipse.collections.api.RichIterable;

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
	 * Set some metadata that is only available in runtime. This type of
	 * metadata will not be copied when {@link #derive()} is used.
	 *
	 * @param <V>
	 *   type of value
	 * @param key
	 *   key to set
	 * @param value
	 *   value to set
	 */
	<V> void setRuntimeMetadata(MetadataKey<V> key, V value);

	/**
	 * Get all of the metadata that was defined when the type was built.
	 *
	 * @return
	 */
	RichIterable<MetadataDef> getDefinedMetadata();

	/**
	 * Start building a new instance based on this one.
	 *
	 * @return
	 */
	Builder<?> derive();

	interface Builder<B extends Builder<B>>
	{
		/**
		 * Set some metadata.
		 *
		 * @param argument
		 * @return
		 */
		<V> B withMetadata(MetadataKey<V> key, V value);

		/**
		 * Set several metadata keys at once.
		 *
		 * @param defs
		 * @return
		 */
		B withAllMetadata(Iterable<MetadataDef> defs);

		/**
		 * Build the instance.
		 *
		 * @return
		 */
		HasMetadata build();
	}
}

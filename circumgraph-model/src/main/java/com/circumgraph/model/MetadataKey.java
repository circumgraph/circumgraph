package com.circumgraph.model;

/**
 * Metadata key for use with things that implements {@link HasMetadata}.
 */
public class MetadataKey<V>
{
	private final String id;

	private MetadataKey(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	public static <V> MetadataKey<V> create(String id, Class<V> type)
	{
		return new MetadataKey<>(id);
	}
}

package com.circumgraph.model.internal;

import com.circumgraph.model.MetadataDef;

/**
 * Implementation of {@link MetadataDef}.
 */
public class MetadataDefImpl
	implements MetadataDef
{
	private final String key;
	private final Object value;

	public MetadataDefImpl(String key, Object value)
	{
		this.key = key;
		this.value = value;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public Object getValue()
	{
		return value;
	}
}

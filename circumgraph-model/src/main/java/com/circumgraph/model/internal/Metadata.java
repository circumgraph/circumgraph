package com.circumgraph.model.internal;

import java.util.Objects;
import java.util.Optional;

import com.circumgraph.model.HasMetadata;
import com.circumgraph.model.MetadataDef;
import com.circumgraph.model.MetadataKey;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.tuple.Tuples;

/**
 * Helper used for implementing {@link HasMetadata}.
 */
public class Metadata
{
	private final ImmutableMap<String, Object> definedMetadata;
	private final MutableMap<String, Object> allMetadata;

	public Metadata(ImmutableMap<String, Object> definedMetadata)
	{
		this.definedMetadata = definedMetadata;
		this.allMetadata = Maps.mutable.ofMapIterable(definedMetadata);
	}

	@SuppressWarnings("unchecked")
	public <V> Optional<V> getMetadata(MetadataKey<V> key)
	{
		return Optional.ofNullable((V) allMetadata.get(key.getId()));
	}

	public RichIterable<MetadataDef> getDefinedMetadata()
	{
		return definedMetadata.keyValuesView()
			.collect(p -> new MetadataDefImpl(p.getOne(), p.getTwo()));
	}

	public ImmutableMap<String, Object> getDefinedMetadataMap()
	{
		return definedMetadata;
	}

	public <V> void setRuntimeMetadata(MetadataKey<V> key, V value)
	{
		allMetadata.put(key.getId(), value);
	}

	/**
	 * Create a version of this instance without runtime values.
	 *
	 * @return
	 */
	public Metadata derive()
	{
		return new Metadata(definedMetadata);
	}

	/**
	 * Set some metadata.
	 *
	 * @param argument
	 * @return
	 */
	public <V> Metadata withMetadata(MetadataKey<V> key, V value)
	{
		return new Metadata(definedMetadata.newWithKeyValue(key.getId(), value));
	}

	/**
	 * Set several metadata keys at once.
	 *
	 * @param defs
	 * @return
	 */
	public Metadata withAllMetadata(Iterable<MetadataDef> defs)
	{
		return new Metadata(definedMetadata.newWithAllKeyValues(
			Lists.immutable.ofAll(defs).collect(p -> Tuples.pair(p.getKey(), p.getValue()))
		));
	}

	/**
	 * Create an empty instance.
	 *
	 * @return
	 */
	public static Metadata empty()
	{
		return new Metadata(Maps.immutable.empty());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(definedMetadata);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		Metadata other = (Metadata) obj;
		return Objects.equals(definedMetadata, other.definedMetadata);
	}
}

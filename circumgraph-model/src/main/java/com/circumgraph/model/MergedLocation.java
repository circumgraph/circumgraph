package com.circumgraph.model;

import com.circumgraph.model.internal.MergedLocationImpl;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Version of {@link Location} used to merge several locations into a single
 * one. This is used when types are modified or merged.
 */
public interface MergedLocation
	extends Location
{
	/**
	 * Get all the locations that have been merged together.
	 *
	 * @return
	 */
	ListIterable<Location> list();

	/**
	 * Merge this location with another one. Used when things are modified
	 * dynamically.
	 *
	 * @param other
	 * @return
	 */
	@Override
	MergedLocation mergeWith(Location other);

	/**
	 * Turn a location into a list of locations.
	 *
	 * @param location
	 * @return
	 */
	static ListIterable<Location> toList(Location location)
	{
		return location instanceof MergedLocation m
			? m.list()
			: Lists.immutable.of(location);
	}

	/**
	 * Get a merged version of all the given locations.
	 *
	 * @param locations
	 * @return
	 */
	static MergedLocation of(Location... locations)
	{
		return new MergedLocationImpl(Lists.immutable.of(locations));
	}
}

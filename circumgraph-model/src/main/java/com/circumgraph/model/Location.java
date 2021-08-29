package com.circumgraph.model;

import com.circumgraph.model.internal.LocationImpl;
import com.circumgraph.model.internal.MergedLocationImpl;

import org.eclipse.collections.api.factory.Lists;

/**
 * Representation of a location.
 */
@FunctionalInterface
public interface Location
{
	/**
	 * Turn this object into a developer-friendly message describing the
	 * location.
	 */
	String describe();

	/**
	 * Merge this location with another one. Used when things are modified
	 * dynamically.
	 *
	 * @param other
	 * @return
	 */
	default MergedLocation mergeWith(Location other)
	{
		return new MergedLocationImpl(Lists.immutable.of(this, other));
	}

	/**
	 * Create a location representing a position in the code. This will attempt
	 * to find the first non-model package and use it as the source.
	 *
	 * @return
	 *   instance representing the best known location
	 */
	static Location code()
	{
		return LocationImpl.code();
	}

	/**
	 * Get the best location available. Will use the
	 * {@link #scope(Location) scoped location} and fallback on {@link #code()}
	 * if no active location.
	 *
	 * @param picked
	 *   location to maybe use, may be {@code null}
	 * @return
	 *   instance representing the best known location, never {@code null}
	 */
	static Location automatic()
	{
		return Location.automatic(null);
	}

	/**
	 * Get the best location available. Will use the picked location first,
	 * falling back to the {@link #scope(Location) scoped location} and
	 * {@link #code()} if no active location.
	 *
	 * @param picked
	 *   location to maybe use, may be {@code null}
	 * @return
	 *   instance representing the best known location, never {@code null}
	 */
	static Location automatic(Location picked)
	{
		return LocationImpl.automatic(picked);
	}

	/**
	 * Create a location from the given message.
	 *
	 * @param message
	 * @return
	 */
	static Location create(String message)
	{
		return new LocationImpl(message);
	}

	/**
	 * Get an object representing an unknown location.
	 *
	 * @return
	 */
	static Location unknown()
	{
		return LocationImpl.UNKNOWN;
	}

	/**
	 * Scope operations on the current thread to the given location. Should be
	 * used with a try-with-resources statement.
	 *
	 * <pre>
	 * try(var handle = Location.scope(currentLocation)) {
	 *    ...
	 * }
	 * </pre>
	 *
	 * @param location
	 * @return
	 */
	static Handle scope(Location location)
	{
		return LocationImpl.scope(location);
	}

	/**
	 * Handle returned by {@link Location#scope(Location)}. Used to remove
	 * the scope when done.
	 */
	@FunctionalInterface
	interface Handle
		extends AutoCloseable
	{
		@Override
		void close();
	}
}

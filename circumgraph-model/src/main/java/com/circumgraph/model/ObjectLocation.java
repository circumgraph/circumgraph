package com.circumgraph.model;

/**
 * Variant of {@link Location} to simplify creating locations that
 * describe a position in an object.
 */
public interface ObjectLocation
	extends Location
{
	default ObjectLocation forField(String name)
	{
		return () -> {
			var current = describe();
			return current.isEmpty() ? name : current + '.' + name;
		};
	}

	default ObjectLocation forIndex(int idx)
	{
		return () -> describe() + '[' + idx + ']';
	}

	static ObjectLocation root()
	{
		return () -> "";
	}
}

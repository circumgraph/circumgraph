package com.circumgraph.storage.internal.mappers;

import com.circumgraph.model.validation.SourceLocation;

/**
 * Variant of {@link SourceLocation} to simplify creating locations that
 * describe a position in a mutation.
 */
public interface ObjectLocation
	extends SourceLocation
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

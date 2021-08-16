package com.circumgraph.model.validation;

import org.eclipse.collections.api.factory.Lists;

@FunctionalInterface
public interface MergedSourceLocation
	extends SourceLocation
{
	@Override
	default String describe()
	{
		var builder = new StringBuilder();

		for(var loc : list())
		{
			if(builder.isEmpty())
			{
				builder.append(loc.describe());
			}
			else
			{
				builder
					.append(" modified by ")
					.append(loc.describe());
			}
		}

		return builder.toString();
	}

	/**
	 * Get all the locations that have been merged together.
	 *
	 * @return
	 */
	Iterable<SourceLocation> list();

	/**
	 * Merge this location with another one. Used when things are modified
	 * dynamically.
	 *
	 * @param other
	 * @return
	 */
	@Override
	default MergedSourceLocation mergeWith(SourceLocation other)
	{
		var merged = Lists.immutable.ofAll(list()).newWith(other);
		return () -> merged;
	}
}

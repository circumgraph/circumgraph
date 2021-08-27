package com.circumgraph.model.internal;

import java.util.Objects;

import com.circumgraph.model.Location;
import com.circumgraph.model.MergedLocation;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

/**
 * Implementation of {@link MergedLocation}.
 */
public class MergedLocationImpl
	implements MergedLocation
{
	private final ImmutableList<Location> locations;

	public MergedLocationImpl(
		ImmutableList<Location> locations
	)
	{
		this.locations = locations;
	}

	@Override
	public String describe()
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

	@Override
	public ListIterable<Location> list()
	{
		return locations;
	}

	@Override
	public MergedLocation mergeWith(Location other)
	{
		return new MergedLocationImpl(locations.newWith(other));
	}

	@Override
	public String toString()
	{
		return "MergedLocation{" + locations + "}";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(locations);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		MergedLocationImpl other = (MergedLocationImpl) obj;
		return Objects.equals(locations, other.locations);
	}
}

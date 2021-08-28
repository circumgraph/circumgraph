package com.circumgraph.model.internal;

import java.util.Objects;

import com.circumgraph.model.Location;

public class LocationImpl
	implements Location
{
	public static final Location UNKNOWN = new LocationImpl("Unknown Location");

	private final String message;

	public LocationImpl(String message)
	{
		this.message = message;
	}

	@Override
	public String toString()
	{
		return message;
	}

	@Override
	public String describe()
	{
		return message;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(message);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		LocationImpl other = (LocationImpl) obj;
		return Objects.equals(message, other.message);
	}
}

package com.circumgraph.model.internal;

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
}

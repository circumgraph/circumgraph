package com.circumgraph.model.internal;

import com.circumgraph.model.validation.SourceLocation;

public class BasicSourceLocation
	implements SourceLocation
{
	public static final SourceLocation UNKNOWN = new BasicSourceLocation("Unknown Location");

	private final String message;

	public BasicSourceLocation(String message)
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

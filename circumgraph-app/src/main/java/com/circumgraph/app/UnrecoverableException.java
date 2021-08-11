package com.circumgraph.app;

/**
 * Indicate an error that can't be automatically recovered from.
 */
public class UnrecoverableException
	extends RuntimeException
{
	public UnrecoverableException(String message)
	{
		super(message);
	}

	public UnrecoverableException(String message, Throwable cause)
	{
		super(message, cause);
	}
}

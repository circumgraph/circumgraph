package com.circumgraph.app;

/**
 * Exception used for errors that may be temporary.
 */
public class MaybeTemporaryException
	extends RuntimeException
{
	public MaybeTemporaryException(String message)
	{
		super(message);
	}

	public MaybeTemporaryException(String message, Throwable cause)
	{
		super(message, cause);
	}
}

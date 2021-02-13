package com.circumgraph.model;

/**
 * Exception thrown when an issue with the model is found.
 */
public class ModelException
	extends RuntimeException
{
	public ModelException(String message)
	{
		super(message);
	}

	public ModelException(
		String message,
		Throwable cause
	)
	{
		super(message, cause);
	}
}

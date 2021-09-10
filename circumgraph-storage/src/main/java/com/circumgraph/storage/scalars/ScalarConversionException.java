package com.circumgraph.storage.scalars;

/**
 * Exception used by {@link Scalar scalars} when conversion can not be
 * performed.
 */
public class ScalarConversionException
	extends RuntimeException
{
	public ScalarConversionException(String message)
	{
		super(message);
	}

	public ScalarConversionException(Throwable cause)
	{
		super(cause);
	}

	public ScalarConversionException(
		String message,
		Throwable cause
	)
	{
		super(message, cause);
	}
}

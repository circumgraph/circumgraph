package com.circumgraph.storage;

/**
 * Exception thrown when a search fails.
 */
public class StorageSearchException
	extends StorageException
{
	public StorageSearchException(String message)
	{
		super(message);
	}

	public StorageSearchException(
		String message,
		Throwable cause
	)
	{
		super(message, cause);
	}
}

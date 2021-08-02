package com.circumgraph.storage;

/**
 * Exception thrown when issues occur in the storage, thrown both during
 * mutation and fetching.
 */
public class StorageException
	extends RuntimeException
{
	public StorageException(String message)
	{
		super(message);
	}

	public StorageException(
		String message,
		Throwable cause
	)
	{
		super(message, cause);
	}
}

package com.circumgraph.storage;

import com.circumgraph.model.validation.ValidationMessage;

import org.eclipse.collections.api.list.ListIterable;

public class StorageValidationException
	extends StorageException
{
	public StorageValidationException(ListIterable<ValidationMessage> issues)
	{
		super("Unable to store, validation failed:\n" +
			issues
				.collect(msg -> "  * " + msg.getLocation().describe() + ": " + msg.getMessage())
				.makeString("\n")
		);
	}
}

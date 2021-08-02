package com.circumgraph.storage;

import com.circumgraph.model.validation.ValidationMessage;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Exception thrown when a validation issues occurs during mutation of an
 * object.
 */
public class StorageValidationException
	extends StorageException
{
	private final ListIterable<ValidationMessage> issues;

	public StorageValidationException(ListIterable<ValidationMessage> issues)
	{
		super("Unable to store, validation failed:\n" +
			issues
				.collect(msg -> "  * " + msg.getLocation().describe() + ": " + msg.getMessage())
				.makeString("\n")
		);

		this.issues = issues;
	}

	/**
	 * Get issues reported.
	 *
	 * @return
	 */
	public ListIterable<ValidationMessage> getIssues()
	{
		return issues;
	}
}

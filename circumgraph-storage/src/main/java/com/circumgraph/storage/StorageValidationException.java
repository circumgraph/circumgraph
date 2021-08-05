package com.circumgraph.storage;

import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageLevel;

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
				.select(m -> m.getLevel() == ValidationMessageLevel.ERROR)
				.collect(msg -> "  * " + msg.format())
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

package com.circumgraph.model;

import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.eclipse.collections.api.list.ListIterable;

/**
 * Exception thrown when a model fails to validate during construction.
 */
public class ModelValidationException
	extends ModelException
{
	private final ListIterable<ValidationMessage> issues;

	public ModelValidationException(ListIterable<ValidationMessage> issues)
	{
		this("Invalid model, errors reported:", issues);
	}

	public ModelValidationException(String prefix, ListIterable<ValidationMessage> issues)
	{
		super(prefix + "\n" +
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

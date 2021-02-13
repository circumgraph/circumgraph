package com.circumgraph.model.validation;

import java.util.regex.Pattern;

/**
 * Validation utilities for models. Provided as a public API to let creators
 * of {@link com.circumgraph.model.Schema} perform some early validation.
 */
public class ModelValidation
{
	private static final Pattern NAME_PATTERN = Pattern.compile("^[_a-zA-Z][_a-zA-Z0-9]*$");

	private ModelValidation()
	{
	}

	/**
	 * Make sure that the given name is valid for a field, or throw an
	 * exception.
	 *
	 * @param name
	 */
	public static void requireValidFieldName(String name)
	{
		if(! isValidFieldName(name))
		{
			throw new IllegalArgumentException("Invalid field name");
		}
	}

	/**
	 * Check if the given field name is valid.
	 *
	 * @param name
	 *   name to check
	 * @return
	 *   {@code true} if valid, {@code false} otherwise
	 */
	public static boolean isValidFieldName(String name)
	{
		return name != null && ! name.startsWith("__") && NAME_PATTERN.matcher(name).matches();
	}
}

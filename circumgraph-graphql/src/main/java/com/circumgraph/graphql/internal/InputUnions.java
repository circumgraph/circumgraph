package com.circumgraph.graphql.internal;

import java.util.Map;

import com.circumgraph.model.InputObjectDef;

import graphql.ErrorType;
import graphql.GraphqlErrorException;

/**
 * Helpers for working with input unions.
 */
public class InputUnions
{
	private InputUnions()
	{
	}

	/**
	 * Validate a type so that it contains exactly one field.
	 *
	 * @param type
	 *   type being validated
	 * @param map
	 *   map of data
	 */
	public static void validate(InputObjectDef type, Map<String, Object> map)
	{
		int c = 0;
		for(var e : map.entrySet())
		{
			if(e.getValue() != null)
			{
				c++;

				if(c > 1)
				{
					// More than one field has been used - throw an exception
					throw GraphqlErrorException.newErrorException()
						.errorClassification(ErrorType.ValidationError)
						.message("Only a single field can be used in " + type.getName())
						.build();
				}
			}
		}
	}
}

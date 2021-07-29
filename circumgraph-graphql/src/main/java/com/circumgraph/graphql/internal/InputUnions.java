package com.circumgraph.graphql.internal;

import java.util.Map;

import graphql.ErrorType;
import graphql.GraphqlErrorException;
import graphql.schema.GraphQLInputObjectType;

/**
 * Helpers for working with input unions.
 */
public class InputUnions
{
	private InputUnions()
	{
	}

	public static void validate(GraphQLInputObjectType type, Map<String, Object> map)
	{
		int c = 0;
		for(var e : map.entrySet())
		{
			if(e.getValue() != null)
			{
				c++;

				if(c > 1)
				{
					throw GraphqlErrorException.newErrorException()
						.errorClassification(ErrorType.ValidationError)
						.message("Only a single field can be used in " + type.getName())
						.build();
				}
			}
		}
	}
}

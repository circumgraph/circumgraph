package com.circumgraph.graphql.internal;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.model.StructuredDef;

/**
 * Utils for generating names of different parts of the schema.
 */
public class SchemaNames
{
	private SchemaNames()
	{
	}

	/**
	 * Get the name the given field should have in an output type.
	 *
	 * @param def
	 *   field to get name for
	 * @return
	 *   name of the field
	 */
	public static String toOutputFieldName(FieldDef def)
	{
		// TODO: Support for a custom field name
		return def.getName();
	}

	/**
	 * Get the name the given type should have in an output type used for
	 * querying.
	 *
	 * @param def
	 *   type to get name for
	 * @return
	 *   name of query field
	 */
	public static String toQueryFieldName(StructuredDef def)
	{
		// TODO: Support for controlling the name of the field in query
		return toQueryFieldName(def.getName());
	}

	/**
	 * Get the name the given type should have in an output type used for
	 * querying.
	 *
	 * @param typeName
	 *   original name, assumed to be in UpperCamel
	 * @return
	 *   converted name in lowerCamel
	 */
	public static String toQueryFieldName(String typeName)
	{
		return toLowerCamel(typeName);
	}

	/**
	 * Get the name the given type would have in an input object.
	 *
	 * @param def
	 *   type to get name for
	 * @return
	 *   name of input field
	 */
	public static String toInputFieldName(OutputTypeDef def)
	{
		return toLowerCamel(def.getName());
	}

	/**
	 * Get the name the given type would have in an input object.
	 *
	 * @param def
	 *   type to get name for
	 * @return
	 *   name of input field
	 */
	public static String toInputFieldName(FieldDef def)
	{
		return def.getName();
	}

	/**
	 * Get the name the query object associated with the type should have.
	 *
	 * @param def
	 * @return
	 */
	public static String toQueryObjectName(StructuredDef def)
	{
		// TODO: Support for controlling the type name
		return def.getName() + "Query";
	}

	/**
	 * Get the name of the field used to store the given type.
	 *
	 * @param def
	 * @return
	 */
	public static String toStoreMutationFieldName(StructuredDef def)
	{
		// TODO: Support for controlling the type name
		return "store" + def.getName();
	}

	/**
	 * Get the name of the field used to store the given type.
	 *
	 * @param def
	 * @return
	 */
	public static String toDeleteMutationFieldName(StructuredDef def)
	{
		// TODO: Support for controlling the type name
		return "delete" + def.getName();
	}

	/**
	 * Get the name the mutation input for this type.
	 *
	 * @param def
	 * @return
	 */
	public static String toMutationInputTypeName(OutputTypeDef def)
	{
		// TODO: Support for controlling the type name
		return def.getName() + "MutationInput";
	}

	/**
	 * Get the name the mutation input for this type.
	 *
	 * @param def
	 * @return
	 */
	public static String toRefInputTypeName(StructuredDef def)
	{
		// TODO: Support for controlling the type name
		return def.getName() + "RefInput";
	}

	/**
	 * Get the name of search results for the given type.
	 *
	 * @param def
	 * @return
	 */
	public static String toSearchResultTypeName(StructuredDef def)
	{
		// TODO: Support for controlling the type name
		return def.getName() + "SearchResult";
	}

	/**
	 * Get the name of the sorting enum used for the given type.
	 *
	 * @param def
	 * @return
	 */
	public static String toSearchEdgeTypeName(StructuredDef def)
	{
		// TODO: Support for controlling the type name
		return def.getName() + "Edge";
	}


	/**
	 * Get the name of the sorting enum used for the given type.
	 *
	 * @param def
	 * @return
	 */
	public static String toSortEnumName(StructuredDef def)
	{
		// TODO: Support for controlling the type name
		return def.getName() + "Sort";
	}

	/**
	 * Get the name of an enum value based on the field name.
	 *
	 * @param def
	 * @return
	 */
	public static String toSortEnumValue(FieldDef def)
	{
		return toUpperSnakeCase(def.getName());
	}

	/**
	 * Get the name of the sorting enum used for the given type.
	 *
	 * @param def
	 * @return
	 */
	public static String toSortInputName(StructuredDef def)
	{
		// TODO: Support for controlling the type name
		return def.getName() + "SortInput";
	}

	/**
	 * Turn a name into a lower camel case.
	 *
	 * @param name
	 *   name to convert
	 * @return
	 *   lower camel version of name
	 */
	static String toLowerCamel(String name)
	{
		for(int i=0, n=name.length(); i<n; i++)
		{
			var c = name.charAt(i);
			if(! Character.isLowerCase(c) && ! Character.isDigit(c)) continue;

			/*
			 * Switched to lowercase or digits, the previous character is the
			 * end of the first segment of the name.
			 */
			switch(i)
			{
				case 0:
					// Started with a lower case char - return string as is
					return name;
				case 1:
					// First character is a digit or upper case - lower case it
					return Character.toLowerCase(name.charAt(0)) + name.substring(1);
				default:
					// Switched somewhere in the middle of the string
					return name.substring(0, i - 1).toLowerCase() + name.substring(i - 1);
			}
		}

		return name.toLowerCase();
	}

	/**
	 * Turn a name into upper snake case.
	 *
	 * @param name
	 *   name to convert
	 * @return
	 *   upper snake case version of name
	 */
	static String toUpperSnakeCase(String name)
	{
		var builder = new StringBuilder();

		var last = 0;
		var next = 0;
		while((next = findNextBoundary(name, last)) != -1)
		{
			/*
			 * Switched type of case - treat the previous character as the end
			 * of the segment.
			 */
			if(builder.length() != 0)
			{
				builder.append('_');
			}

			builder.append(name.substring(last, next).toUpperCase());
			last = next;
		}

		return builder.toString();
	}

	/**
	 * Find the next boundary in the given string.
	 *
	 * @param v
	 * @param start
	 */
	private static int findNextBoundary(String v, int start)
	{
		var n = v.length();
		for(int i=start + 2; i<n; i++)
		{
			var c0 = v.charAt(i - 1);
			var c1 = v.charAt(i);

			if(Character.isDigit(c1))
			{
				// Switching from upper or lower case to digit
				if(! Character.isDigit(c0))
				{
					return i;
				}
			}
			else if(Character.isLowerCase(c1))
			{
				// Switching from digit or upper case to lower case
				if(Character.isUpperCase(c0))
				{
					// Switching from upper case
					return i - 1;
				}
				else if(Character.isDigit(c0))
				{
					// Switching from a digit
					return i;
				}
			}
			else if(Character.isUpperCase(c1))
			{
				// Switching from digit or lower to upper case
				if(! Character.isUpperCase(c0))
				{
					return i;
				}
			}
		}

		// Return one last boundary which is the end of the string
		return start >= n ? -1 : n;
	}
}

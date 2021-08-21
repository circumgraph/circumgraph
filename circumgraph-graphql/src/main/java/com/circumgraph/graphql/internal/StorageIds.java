package com.circumgraph.graphql.internal;

import graphql.ErrorClassification;
import graphql.GraphqlErrorException;
import se.l4.ylem.ids.Base62LongIdCodec;
import se.l4.ylem.ids.LongIdCodec;

/**
 * Utility for encoding/decoding ids. The GraphQL API uses strings for
 * identifiers while the storage layers stores these as longs, this is used
 * for conversion for querying and mutations.
 */
public class StorageIds
{
	private static final LongIdCodec<String> CODEC = new Base62LongIdCodec();

	private StorageIds()
	{
	}

	/**
	 * Encode the given identifier into a string.
	 *
	 * @param id
	 * @return
	 */
	public static String encode(long id)
	{
		return CODEC.encode(id);
	}

	/**
	 * Decode the given string into an identifier.
	 *
	 * @param id
	 * @return
	 */
	public static long decode(String id)
	{
		if(id == null)
		{
			throw GraphqlErrorException.newErrorException()
				.message("Invalid identifier")
				.build();
		}

		try
		{
			return CODEC.decode(id);
		}
		catch(NumberFormatException e)
		{
			throw GraphqlErrorException.newErrorException()
				.message("Invalid identifier")
				.build();
		}
	}
}

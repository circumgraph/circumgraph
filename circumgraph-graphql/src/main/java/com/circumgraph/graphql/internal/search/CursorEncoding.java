package com.circumgraph.graphql.internal.search;

import java.util.Base64;

import com.circumgraph.storage.search.Cursor;

/**
 * Helper for working with encoding and decoding {@link Cursor}s.
 */
public class CursorEncoding
{
	private CursorEncoding()
	{
	}

	public static String encode(Cursor c)
	{
		return Base64.getUrlEncoder().encodeToString(c.encode());
	}

	public static Cursor decode(String in)
	{
		if(in == null) return null;

		byte[] encoded = Base64.getUrlDecoder().decode(in);
		return Cursor.from(encoded);
	}
}

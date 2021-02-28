package com.circumgraph.storage.search;

import com.circumgraph.storage.internal.search.OffsetBasedCursor;

/**
 * Cursor representing the location in some results.
 */
public interface Cursor
{
	/**
	 * Encode this cursor.
	 *
	 * @return
	 */
	byte[] encode();

	static Cursor from(byte[] data)
	{
		switch(data[0])
		{
			case 0:
				// Offset based
				return OffsetBasedCursor.from(data);
			default:
				throw new IllegalArgumentException();
		}
	}
}

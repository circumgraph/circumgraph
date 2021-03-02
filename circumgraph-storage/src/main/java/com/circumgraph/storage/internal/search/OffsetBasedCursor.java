package com.circumgraph.storage.internal.search;

import java.util.Objects;

import com.circumgraph.storage.search.Cursor;

/**
 * Offset-based cursor.
 */
public class OffsetBasedCursor
	implements Cursor
{
	private final int offset;

	public OffsetBasedCursor(int offset)
	{
		this.offset = offset;
	}

	public int getOffset()
	{
		return offset;
	}

	@Override
	public byte[] encode()
	{
		byte[] data = new byte[5];

		data[0] = 0;
		data[1] = (byte) (offset & 0xff);
		data[2] = (byte) ((offset >> 8) & 0xff);
		data[3] = (byte) ((offset >> 16) & 0xff);
		data[4] = (byte) ((offset >> 24) & 0xff);

		return data;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(offset);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		OffsetBasedCursor other = (OffsetBasedCursor) obj;
		return offset == other.offset;
	}

	@Override
	public String toString()
	{
		return "OffsetBasedCursor{offset=" + offset + "}";
	}

	public static Cursor from(byte[] data)
	{
		int offset = (data[1] & 0xff) |
			(data[2] & 0xff) << 8 |
			(data[3] & 0xff) << 16 |
			(data[3] & 0xff) << 24;

		return new OffsetBasedCursor(offset);
	}
}

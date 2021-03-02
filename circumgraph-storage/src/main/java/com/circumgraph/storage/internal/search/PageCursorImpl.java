package com.circumgraph.storage.internal.search;

import java.util.Objects;

import com.circumgraph.storage.search.Cursor;
import com.circumgraph.storage.search.PageCursor;

public class PageCursorImpl
	implements PageCursor
{
	private final Cursor cursor;
	private final int pageNumber;
	private final boolean current;

	public PageCursorImpl(
		Cursor cursor,
		int pageNumber,
		boolean current
	)
	{
		this.cursor = cursor;
		this.pageNumber = pageNumber;
		this.current = current;
	}

	@Override
	public Cursor getCursor()
	{
		return cursor;
	}

	@Override
	public int getPageNumber()
	{
		return pageNumber;
	}

	@Override
	public boolean isCurrent()
	{
		return current;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(current, cursor, pageNumber);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		PageCursorImpl other = (PageCursorImpl) obj;
		return current == other.current
			&& Objects.equals(cursor, other.cursor)
			&& pageNumber == other.pageNumber;
	}

	@Override
	public String toString()
	{
		return "PageCursor{current=" + current
			+ ", cursor=" + cursor
			+ ", pageNumber=" + pageNumber
			+ "}";
	}


}

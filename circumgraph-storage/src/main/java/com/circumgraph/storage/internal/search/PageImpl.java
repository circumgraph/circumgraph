package com.circumgraph.storage.internal.search;

import java.util.Optional;

import com.circumgraph.storage.search.Cursor;
import com.circumgraph.storage.search.Page;

public class PageImpl
	implements Page
{
	private final int limit;
	private final Cursor cursor;

	public PageImpl(
		int limit,
		Cursor cursor
	)
	{
		this.limit = limit;
		this.cursor = cursor;
	}

	@Override
	public int getLimit()
	{
		return limit;
	}

	@Override
	public Optional<Cursor> getCursor()
	{
		return Optional.ofNullable(cursor);
	}
}

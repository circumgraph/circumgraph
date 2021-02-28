package com.circumgraph.storage.internal.search;

import com.circumgraph.storage.search.Cursor;
import com.circumgraph.storage.search.PageInfo;

public class PageInfoImpl
	implements PageInfo
{
	private final boolean nextPage;
	private final boolean previousPage;
	private final Cursor startCursor;
	private final Cursor endCursor;

	public PageInfoImpl(
		boolean nextPage,
		boolean previousPage,
		Cursor startCursor,
		Cursor endCursor
	)
	{
		this.nextPage = nextPage;
		this.previousPage = previousPage;
		this.startCursor = startCursor;
		this.endCursor = endCursor;
	}

	@Override
	public boolean hasNextPage()
	{
		return nextPage;
	}

	@Override
	public boolean hasPreviousPage()
	{
		return previousPage;
	}

	@Override
	public Cursor getStartCursor()
	{
		return startCursor;
	}

	@Override
	public Cursor getEndCursor()
	{
		return endCursor;
	}
}

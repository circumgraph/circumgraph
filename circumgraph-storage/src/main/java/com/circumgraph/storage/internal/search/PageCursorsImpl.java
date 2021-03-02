package com.circumgraph.storage.internal.search;

import com.circumgraph.storage.search.PageCursor;
import com.circumgraph.storage.search.PageCursors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.list.primitive.IntInterval;

public class PageCursorsImpl
	implements PageCursors
{
	private final ImmutableList<PageCursor> start;
	private final ImmutableList<PageCursor> middle;
	private final ImmutableList<PageCursor> end;

	private final PageCursor previous;
	private final PageCursor next;

	public PageCursorsImpl(
		int offset,
		int totalSize,
		int pageSize,
		int maxSize
	)
	{
		int pages = (int) Math.ceil(totalSize / (double) pageSize);
		int page = (offset / pageSize) + 1;

		if(pages <= maxSize)
		{
			start = IntInterval.fromTo(1, pages)
				.collect(p -> create(pageSize, page, p));

			middle = Lists.immutable.empty();

			end = Lists.immutable.empty();
		}
		else if(page <= ((maxSize / 2) + 1))
		{
			start = IntInterval.fromTo(1, maxSize - 2)
				.collect(p -> create(pageSize, page, p));

			middle = Lists.immutable.empty();

			end = Lists.immutable.of(create(pageSize, page, pages));
		}
		else if(page >= (pages - (maxSize / 2)))
		{
			start = Lists.immutable.of(create(pageSize, page, 1));

			middle = Lists.immutable.empty();

			end = IntInterval.fromTo(pages - (maxSize - 3), pages)
				.collect(p -> create(pageSize, page, p));
		}
		else
		{
			int w = (maxSize - 4) / 2;

			start = Lists.immutable.of(create(pageSize, page, 1));

			middle = IntInterval.fromTo(page - w, page + w)
				.collect(p -> create(pageSize, page, p));

			end = Lists.immutable.of(create(pageSize, page, pages));
		}

		previous = page > 1 ? create(pageSize, page, page - 1) : null;
		next = page < pages ? create(pageSize, page, page + 1) : null;
	}

	private static PageCursor create(int pageSize, int c, int p)
	{
		return new PageCursorImpl(
			new OffsetBasedCursor((p - 1) * pageSize),
			p,
			p == c
		);
	}

	@Override
	public PageCursor getPrevious()
	{
		return previous;
	}

	@Override
	public ListIterable<? extends PageCursor> getStart()
	{
		return start;
	}

	@Override
	public ListIterable<? extends PageCursor> getMiddle()
	{
		return middle;
	}

	@Override
	public ListIterable<? extends PageCursor> getEnd()
	{
		return end;
	}

	@Override
	public PageCursor getNext()
	{
		return next;
	}
}

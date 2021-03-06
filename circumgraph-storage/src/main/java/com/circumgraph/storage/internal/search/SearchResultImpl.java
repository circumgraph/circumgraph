package com.circumgraph.storage.internal.search;

import com.circumgraph.storage.StoredEntityValue;
import com.circumgraph.storage.search.Edge;
import com.circumgraph.storage.search.PageCursors;
import com.circumgraph.storage.search.PageInfo;
import com.circumgraph.storage.search.SearchResult;

import org.eclipse.collections.api.list.ListIterable;

import se.l4.silo.index.search.PaginatedSearchResult;

/**
 * Implementation of {@link SearchResult} on top of a paginated result.
 */
public class SearchResultImpl
	implements SearchResult
{
	private final PaginatedSearchResult<StoredEntityValue> result;
	private final PageInfo pageInfo;

	public SearchResultImpl(
		PaginatedSearchResult<StoredEntityValue> result
	)
	{
		this.result = result;
		pageInfo = new PageInfoImpl(
			result.getTotal() > result.getOffset() + result.getSize(),
			result.getOffset() > 1,
			new OffsetBasedCursor((int) result.getOffset()),
			new OffsetBasedCursor((int) (result.getOffset() + result.getSize()))
		);
	}

	@Override
	public int getTotalCount()
	{
		return (int) result.getTotal();
	}

	@Override
	public boolean isTotalCountEstimated()
	{
		return result.isEstimatedTotal();
	}

	@Override
	public PageInfo getPageInfo()
	{
		return pageInfo;
	}

	@Override
	public PageCursors getPageCursors(int max)
	{
		return new PageCursorsImpl(
			(int) result.getOffset(),
			(int) result.getTotal(),
			(int) result.getLimit(),
			max
		);
	}

	@Override
	public ListIterable<Edge> getEdges()
	{
		int offset = (int) result.getOffset();
		return result
			.getItems()
			.collectWithIndex((hit, i) -> new EdgeImpl(
				hit.score(),
				new OffsetBasedCursor(offset + i),
				hit.item()
			));
	}

	@Override
	public ListIterable<StoredEntityValue> getNodes()
	{
		return getEdges().collect(Edge::getNode);
	}
}

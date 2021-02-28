package com.circumgraph.storage.internal.search;

import com.circumgraph.storage.search.Page;
import com.circumgraph.storage.search.Query;
import com.circumgraph.values.StructuredValue;

import se.l4.silo.index.FieldSort;
import se.l4.silo.index.search.QueryClause;
import se.l4.silo.index.search.SearchIndexQuery;

public class QueryImpl
	implements Query
{
	private SearchIndexQuery.Builder<StructuredValue> query;
	private Page page;

	public QueryImpl()
	{
		query = SearchIndexQuery.create("main", StructuredValue.class);
	}

	@Override
	public Query withPage(Page page)
	{
		this.page = page;
		return this;
	}

	@Override
	public Query withScoresNeeded(boolean scoresNeeded)
	{
		return this;
	}

	@Override
	public Query addClause(QueryClause clause)
	{
		query = query.add(clause);
		return this;
	}

	@Override
	public Query addClauses(Iterable<? extends QueryClause> clauses)
	{
		query = query.add(clauses);
		return this;
	}

	@Override
	public Query addSort(FieldSort sort)
	{
		query = query.sort(sort);
		return this;
	}

	@Override
	public Query addSort(Iterable<? extends FieldSort> sorts)
	{
		for(var sort : sorts)
		{
			query = query.sort(sort);
		}

		return this;
	}

	@Override
	public Query addSort(String field, boolean ascending)
	{
		return addSort(FieldSort.create(field, ascending));
	}

	public SearchIndexQuery.Limited<StructuredValue> buildQuery()
	{
		var limit = page == null ? 10 : page.getLimit();
		var cursor = page == null ? null : page.getCursor().orElse(null);
		return query
			.limited()
			.offset(cursor == null ? 0 : ((OffsetBasedCursor) cursor).getOffset())
			.limit(limit)
			.build();
	}
}

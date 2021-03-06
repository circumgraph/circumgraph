package com.circumgraph.storage.internal.search;

import com.circumgraph.storage.StoredEntityValue;
import com.circumgraph.storage.search.Cursor;
import com.circumgraph.storage.search.Edge;

/**
 * Implementation of {@link Edge}.
 */
public class EdgeImpl
	implements Edge
{
	private final float score;
	private final Cursor cursor;
	private final StoredEntityValue node;

	public EdgeImpl(
		float score,
		Cursor cursor,
		StoredEntityValue node
	)
	{
		this.score = score;
		this.cursor = cursor;
		this.node = node;
	}

	@Override
	public float getScore()
	{
		return score;
	}

	@Override
	public Cursor getCursor()
	{
		return cursor;
	}

	@Override
	public StoredEntityValue getNode()
	{
		return node;
	}
}

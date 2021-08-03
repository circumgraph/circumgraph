package com.circumgraph.storage.internal.indexing;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.types.ValueIndexer;

import se.l4.silo.engine.index.search.types.SearchFieldType;

/**
 * {@link ValueIndexer} for {@link ScalarDef#ID} that indexes the id as a
 * binary values. This allows exact matching and faceting but does not support
 * range queries.
 */
public class IdValueIndexer
	implements ValueIndexer
{
	private static final SearchFieldType<Long> FIELD_TYPE = SearchFieldType.forBinary()
		.map(IdValueIndexer::deserializeId, IdValueIndexer::serializeId)
		.build();

	@Override
	public String getName()
	{
		return "ID";
	}

	@Override
	public SimpleValueDef getType()
	{
		return ScalarDef.ID;
	}

	@Override
	public SearchFieldType<Long> getSearchFieldType()
	{
		return FIELD_TYPE;
	}

	private static final byte[] serializeId(Long id0)
	{
		long id = id0;
		return new byte[] {
			(byte) id,
			(byte) (id >> 8),
			(byte) (id >> 16),
			(byte) (id >> 24),
			(byte) (id >> 32),
			(byte) (id >> 40),
			(byte) (id >> 48),
			(byte) (id >> 56)
		};
	}

	private static final Long deserializeId(byte[] data)
	{
		return ((long) data[7] << 56)
			| ((long) data[6] & 0xff) << 48
			| ((long) data[5] & 0xff) << 40
			| ((long) data[4] & 0xff) << 32
			| ((long) data[3] & 0xff) << 24
			| ((long) data[2] & 0xff) << 16
			| ((long) data[1] & 0xff) << 8
			| ((long) data[0] & 0xff);
	}
}

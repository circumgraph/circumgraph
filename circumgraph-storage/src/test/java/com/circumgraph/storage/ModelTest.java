package com.circumgraph.storage;

import com.circumgraph.model.Model;
import com.circumgraph.model.Schema;

/**
 * Base for tests that create a model from a schema.
 */
public class ModelTest
{
	protected Model createModel(Schema schema)
	{
		return Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addSchema(schema)
			.build();
	}
}

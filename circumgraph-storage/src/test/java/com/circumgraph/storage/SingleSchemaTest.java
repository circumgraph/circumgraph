package com.circumgraph.storage;

import com.circumgraph.model.Model;
import com.circumgraph.model.Schema;

import org.junit.jupiter.api.BeforeEach;

public abstract class SingleSchemaTest
	extends StorageTest
{
	protected Model model;

	@BeforeEach
	public void setup()
	{
		open(createSchema());

		model = storage.getModel();
	}

	protected abstract Schema createSchema();
}

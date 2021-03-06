package com.circumgraph.storage;

import com.circumgraph.model.Model;

import org.junit.jupiter.api.BeforeEach;

public abstract class SingleModelTest
	extends StorageTest
{
	protected Model model;

	@BeforeEach
	public void setup()
	{
		open(model = createModel());
	}

	protected abstract Model createModel();
}

package com.circumgraph.storage;

import java.io.IOException;
import java.nio.file.Path;

import com.circumgraph.model.Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class StorageTest
{
	@TempDir
	Path tmp;

	protected Model model;
	protected Storage storage;

	@BeforeEach
	public void before()
		throws IOException
	{
		model = createModel();

		storage = Storage.open(model, tmp)
			.start()
			.block();
	}

	@AfterEach
	public void after()
		throws Exception
	{
		if(storage != null)
		{
			storage.close();
		}
	}

	protected abstract Model createModel();
}

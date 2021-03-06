package com.circumgraph.storage;

import java.nio.file.Path;

import com.circumgraph.model.Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class StorageTest
{
	@TempDir
	Path tmp;

	protected Storage storage;

	@AfterEach
	public void after()
		throws Exception
	{
		if(storage != null)
		{
			storage.close();
		}
	}

	protected Storage open(Model model)
	{
		if(storage != null)
		{
			throw new AssertionError("Can not open multiple storages in the same test");
		}

		storage = Storage.open(model, tmp)
			.start()
			.block();

		return storage;
	}
}

package com.circumgraph.app;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import com.circumgraph.app.config.ConfigConfig;
import com.circumgraph.app.config.InstanceConfig;
import com.circumgraph.app.config.StorageConfig;

import org.junit.jupiter.api.Test;

public class BasicInstanceTest
	extends InstanceTest
{
	@Test
	public void testEmpty()
		throws IOException
	{
		var config = new InstanceConfig();
		config.setConfig(defaultConfigConfig());
		config.setStorage(defaultStorageConfig());
		config.setHttp(defaultHTTPConfig());

		assertThrows(UnrecoverableException.class, () -> Instance.start(config));
	}

	@Test
	public void testSingleEntity()
		throws IOException
	{
		var config = new InstanceConfig();
		config.setConfig(defaultConfigConfig());
		config.setStorage(defaultStorageConfig());
		config.setHttp(defaultHTTPConfig());

		writeSchema("""
			type Test implements Entity {
				name: String!
			}
		""");

		var instance = Instance.start(config);
		instance.close();
	}

	@Test
	public void testInvalidSchema()
		throws IOException
	{
		var config = new InstanceConfig();
		config.setConfig(defaultConfigConfig());
		config.setStorage(defaultStorageConfig());
		config.setHttp(defaultHTTPConfig());

		writeSchema("""
			type Test implements Entity {
				name: S
			}
		""");

		assertThrows(UnrecoverableException.class, () -> Instance.start(config));
	}

	@Test
	public void testStorageDirDoesNotExist()
		throws IOException
	{
		var config = new InstanceConfig();
		config.setConfig(defaultConfigConfig());
		config.setHttp(defaultHTTPConfig());

		var storageConfig = new StorageConfig();
		storageConfig.setDir(Path.of("nonexistent"));
		config.setStorage(storageConfig);

		assertThrows(UnrecoverableException.class, () -> Instance.start(config));
	}

	@Test
	public void testConfigDirDoesNotExist()
		throws IOException
	{
		var config = new InstanceConfig();
		config.setStorage(defaultStorageConfig());
		config.setHttp(defaultHTTPConfig());

		var configConfig = new ConfigConfig();
		configConfig.setDir(Path.of("nonexistent"));
		config.setConfig(configConfig);

		assertThrows(UnrecoverableException.class, () -> Instance.start(config));
	}
}

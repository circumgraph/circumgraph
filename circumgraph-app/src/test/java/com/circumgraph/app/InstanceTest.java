package com.circumgraph.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.circumgraph.app.config.ConfigConfig;
import com.circumgraph.app.config.HTTPConfig;
import com.circumgraph.app.config.HTTPServerConfig;
import com.circumgraph.app.config.StorageConfig;

import org.junit.jupiter.api.io.TempDir;

public class InstanceTest
{
	@TempDir
	Path tmp;

	protected Path dir(String name)
		throws IOException
	{
		var dir = tmp.resolve(name);
		if(Files.notExists(dir))
		{
			Files.createDirectories(dir);
		}
		return dir;
	}

	protected Path dataDir()
		throws IOException
	{
		return dir("data");
	}

	protected Path configDir()
		throws IOException
	{
		return dir("config");
	}

	protected void writeSchema(String schema)
		throws IOException
	{
		var path = configDir().resolve("schema.gql");
		Files.writeString(path, schema, StandardCharsets.UTF_8);
	}

	protected StorageConfig defaultStorageConfig()
		throws IOException
	{
		var config = new StorageConfig();
		config.setDir(dataDir());
		return config;
	}

	protected ConfigConfig defaultConfigConfig()
		throws IOException
	{
		var config = new ConfigConfig();
		config.setDir(configDir());
		return config;
	}

	protected HTTPConfig defaultHTTPConfig()
	{
		var config = new HTTPConfig();

		var serverConfig = new HTTPServerConfig();
		serverConfig.setPort(3435);
		config.setServer(serverConfig);
		return config;
	}
}

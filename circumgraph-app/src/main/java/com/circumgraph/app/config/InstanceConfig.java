package com.circumgraph.app.config;

import javax.validation.Valid;

import reactor.util.annotation.NonNull;
import se.l4.exobytes.AnnotationSerialization;
import se.l4.exobytes.Expose;

/**
 * Configuration for a Circumgraph instance.
 */
@AnnotationSerialization
public class InstanceConfig
{
	@Expose
	private String env;

	@Expose
	@NonNull @Valid
	private ConfigConfig config = new ConfigConfig();

	@Expose
	@NonNull @Valid
	private StorageConfig storage = new StorageConfig();

	@Expose
	@NonNull @Valid
	private HTTPConfig http = new HTTPConfig();

	public boolean isDev()
	{
		return "dev".equalsIgnoreCase(env) || "development".equalsIgnoreCase(env);
	}

	public ConfigConfig getConfig()
	{
		return config;
	}

	public StorageConfig getStorage()
	{
		return storage;
	}

	public HTTPConfig getHttp()
	{
		return http;
	}
}

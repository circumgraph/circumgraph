package com.circumgraph.app.config;

import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import se.l4.exobytes.AnnotationSerialization;
import se.l4.exobytes.Expose;

/**
 * Configuration related to the schema used to configure the server.
 */
@AnnotationSerialization
public class ConfigConfig
{
	@Expose
	@NotNull
	private Path dir = Path.of("/config");

	public Path getDir()
	{
		return dir;
	}

	public void setDir(Path dir)
	{
		this.dir = dir;
	}
}

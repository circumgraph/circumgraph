package com.circumgraph.app.config;

import java.nio.file.Path;

import reactor.util.annotation.NonNull;
import se.l4.exobytes.AnnotationSerialization;
import se.l4.exobytes.Expose;

/**
 * Configuration for storage.
 */
@AnnotationSerialization
public class StorageConfig
{
	@Expose
	@NonNull
	private Path dir = Path.of("/data");

	public Path getDir()
	{
		return dir;
	}
}

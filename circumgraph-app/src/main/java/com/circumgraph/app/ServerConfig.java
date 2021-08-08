package com.circumgraph.app;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import se.l4.exobytes.AnnotationSerialization;
import se.l4.exobytes.Expose;

@AnnotationSerialization
public class ServerConfig
{
	@Expose
	@Min(1) @Max(65535)
	private int port = 8080;

	public int getPort()
	{
		return port;
	}
}

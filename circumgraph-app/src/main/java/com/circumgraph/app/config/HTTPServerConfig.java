package com.circumgraph.app.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import se.l4.exobytes.AnnotationSerialization;
import se.l4.exobytes.Expose;

/**
 * Configuration for the HTTP server.
 */
@AnnotationSerialization
public class HTTPServerConfig
{
	@Expose
	@Min(1) @Max(65535)
	private int port = 8080;

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}
}

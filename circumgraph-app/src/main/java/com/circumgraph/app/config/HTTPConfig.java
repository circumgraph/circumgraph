package com.circumgraph.app.config;

import javax.validation.Valid;

import reactor.util.annotation.NonNull;
import se.l4.exobytes.AnnotationSerialization;
import se.l4.exobytes.Expose;

/**
 * Container for HTTP config.
 */
@AnnotationSerialization
public class HTTPConfig
{
	@Expose
	@NonNull @Valid
	private HTTPServerConfig server = new HTTPServerConfig();

	public HTTPServerConfig getServer()
	{
		return server;
	}

	public void setServer(HTTPServerConfig server)
	{
		this.server = server;
	}
}

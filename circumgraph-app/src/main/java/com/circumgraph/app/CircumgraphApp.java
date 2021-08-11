package com.circumgraph.app;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.circumgraph.app.config.ConfigConfig;
import com.circumgraph.app.config.InstanceConfig;

import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.exoconf.Config;
import se.l4.exoconf.ConfigException;

/**
 * Main-method that starts up the Circumgraph server.
 */
public class CircumgraphApp
{
	private static final Logger logger = LoggerFactory.getLogger("com.circumgraph");
	private static double MB = 1024 * 1024;

	private static Instance instance;

	public static void main(String[] args)
	{
		logger.info("Starting version " + loadVersion());

		// Log the JVM args
		logger.info("JVM args: {}",
			Lists.immutable.ofAll(ManagementFactory.getRuntimeMXBean().getInputArguments())
				.makeString(" ")
		);

		// Log some information about available memory
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		var heap = memoryBean.getHeapMemoryUsage();
		logger.info(
			"Heap using between {} MiB and {} MiB memory",
			heap.getInit() / MB,
			heap.getMax() / MB
		);

		InstanceConfig config;
		try
		{
			config = Config.create()
				.withRoot(Path.of("."))
				.build()
				.get(InstanceConfig.class)
				.orElseGet(InstanceConfig::new);
		}
		catch(ConfigException e)
		{
			logger.error("Invalid configuration; " + e.getMessage());
			idle();
			return;
		}

		// Setup a thread that will gracefully shutdown the instance
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Shutting down");
			if(instance != null)
			{
				instance.close();
			}
			logger.info("Done");
		}, "shutdown"));

		// Start the instance
		start(config);

		if(config.isDev())
		{
			// Start monitoring for changes
			monitorChanges(config);
		}
	}

	private static String loadVersion()
	{
		Properties props = new Properties();
		try(InputStream in = CircumgraphApp.class.getClassLoader().getResourceAsStream("git.properties"))
		{
			if(in == null) return "<unknown>";

			props.load(in);
		}
		catch(IOException e)
		{
			return "<unknown>";
		}

		String version = props.getProperty("git.commit.id.describe-short");
		return version == null || version.isEmpty()
			? "<unknown>"
			: version;
	}

	/**
	 * Start an instance based on the given configuration. If an instance is
	 * already running it will be shutdown.
	 *
	 * @param config
	 */
	private static void start(InstanceConfig config)
	{
		try
		{
			if(instance != null)
			{
				logger.info("Restarting...");
				instance.close();
			}

			instance = Instance.start(config);
		}
		catch(UnrecoverableException e)
		{
			instance = null;

			logger.error(e.getMessage());

			if(! config.isDev())
			{
				idle();
			}
		}
		catch(MaybeTemporaryException e)
		{
			instance = null;

			logger.error(e.getMessage());
		}
		catch(Exception e)
		{
			instance = null;

			logger.error("Exception caught during start; " + e.getMessage(), e);
		}
	}

	private static void idle()
	{
		logger.warn("Unrecoverable issue detected during startup, server not started");

		while(! Thread.interrupted())
		{
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e)
			{
			}
		}
	}

	private static void monitorChanges(InstanceConfig config)
	{
		logger.info("Development mode, will monitor config for changes");

		var executor = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS);
		CompletableFuture<?> future = null;
		try
		{
			WatchService watchService = FileSystems.getDefault().newWatchService();

			config.getConfig().getDir().register(
				watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY
			);

			WatchKey key;
			while((key = watchService.take()) != null)
			{
				for(var event : key.pollEvents())
				{
					if(event.kind() == StandardWatchEventKinds.OVERFLOW) continue;

					if(future != null && ! future.isCancelled())
					{
						future.cancel(false);
					}

					future = CompletableFuture.runAsync(() -> {
						start(config);
					}, executor);
				}

				key.reset();
			}
		}
		catch(InterruptedException e)
		{
			// Shutting down, no need to do anything
		}
		catch(IOException e)
		{
			logger.warn("Could not monitor for configuration changes; " + e.getMessage(), e);
		}
	}
}

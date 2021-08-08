package com.circumgraph.app;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import com.circumgraph.graphql.GraphQLAPISchema;
import com.circumgraph.graphql.GraphQLGenerator;
import com.circumgraph.model.Model;
import com.circumgraph.model.ModelValidationException;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.schema.graphql.GraphQLSchema;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StorageSchema;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.GraphQL;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import se.l4.exoconf.Config;

public class CircumgraphApp
{
	private static final Logger logger = LoggerFactory.getLogger("com.circumgraph");
	private static double MB = 1024 * 1024;

	public static void main(String[] args)
	{
		logger.info("Starting version " + loadVersion());

		// Log some information about available memory
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		var heap = memoryBean.getHeapMemoryUsage();
		logger.info(
			"Heap using between {} MiB and {} MiB memory",
			heap.getInit() / MB,
			heap.getMax() / MB
		);

		var config = Config.create()
			.build();

		var configDir = config.get("config.dir", String.class).orElse("/config");
		var storageDir = config.get("data.dir", String.class).orElse("/data");

		// Attempt to load the model
		var model = loadModel(Path.of(configDir));
		if(! model.isPresent())
		{
			idle();
			return;
		}

		// Open the storage
		var storage = openStorage(model.get(), Path.of(storageDir));
		if(! storage.isPresent())
		{
			idle();
			return;
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Closing storage");
			storage.get().close();
			logger.info("Storage closed");
		}));

		// Generate and expose GraphQL schema
		try
		{
			var graphQL = createGraphQL(storage.get());

			var vertx = Vertx.vertx();

			var graphQLHandlerOptions = new GraphQLHandlerOptions()
				.setRequestBatchingEnabled(true);

			var graphQLHandler = GraphQLHandler.create(graphQL, graphQLHandlerOptions);

			Router router = Router.router(vertx);
			router.post().handler(BodyHandler.create());
			router.route("/graphql").handler(graphQLHandler);

			var serverConfig = config.get("server", ServerConfig.class).orElseGet(ServerConfig::new);
			var port = serverConfig.getPort();
			vertx.createHttpServer()
				.requestHandler(router)
				.listen(port, http -> {
					if(http.succeeded())
					{
						logger.info("HTTP server started at " + port);
					}
					else
					{
						idle();
					}
				});
		}
		catch(Exception e)
		{
			// Exception while starting, gracefully ask to exit
			logger.error("Unexpected error generating API; " + e.getMessage(), e);
			System.exit(1);
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

	/**
	 * Load the model from the given directory. This will recursively look for
	 * `.graphql` and `.gql` files in the directory and load them.
	 *
	 * @param dir
	 * @return
	 */
	private static Optional<Model> loadModel(Path dir)
	{
		var matching = Lists.mutable.<Path>empty();

		logger.info("Loading config from {}", dir.toAbsolutePath().normalize());

		try
		{
			/**
			 * Find all .gql and .graphql files in the schema directory and load
			 * them.
			 */
			if(Files.isDirectory(dir))
			{

				Files.find(
					dir,
					Integer.MAX_VALUE,
					(filePath, fileAttr) -> {
						var name = filePath.toString();
						return fileAttr.isRegularFile()
							&& (name.endsWith(".gql") || name.endsWith(".graphql"));
					}
				).forEach(matching::add);
			}
			else
			{
				logger.warn("Config directory does not exist, no schema will be loaded");
				return Optional.empty();
			}

			var builder = Model.create()
				.addSchema(StorageSchema.INSTANCE)
				.addSchema(new GraphQLAPISchema());

			for(var file : matching)
			{
				logger.info("Loading " + dir.relativize(file));

				try(var reader = Files.newBufferedReader(file))
				{
					builder = builder.addSchema(
						GraphQLSchema.create(reader)
					);
				}
			}

			var model = builder.build();

			// Log all of the entities that are active
			var entities = model.getImplements(StorageSchema.ENTITY_NAME);
			if(entities.isEmpty())
			{
				logger.error("No entities defined, can not start");
				return Optional.empty();
			}

			logger.info(entities.size() + " entities available:");
			for(var type : entities)
			{
				logger.info("  " + type.getName());
			}

			return Optional.of(model);
		}
		catch(ModelValidationException e)
		{
			logIssues(e.getIssues());
		}
		catch(Exception e)
		{
			logger.error("Failed to load model due to unexpected error; " + e.getMessage(), e);
		}

		return Optional.empty();
	}

	private static Optional<Storage> openStorage(Model model, Path dir)
	{
		logger.info("Storing data in {}", dir.toAbsolutePath().normalize());

		if(! Files.isDirectory(dir))
		{
			logger.warn("Data directory does not exist, storage will not be opened");
			return Optional.empty();
		}

		if(! Files.isWritable(dir))
		{
			logger.warn("Data directory is not writable, storage will not be opened");
			return Optional.empty();
		}

		try
		{
			return Storage.open(model, dir)
				.start()
				.blockOptional();
		}
		catch(Exception e)
		{
			// Exception while starting, gracefully ask to exit
			logger.error("Unexpected error opening storage; " + e.getMessage(), e);
			System.exit(1);
			return Optional.empty();
		}
	}

	private static GraphQL createGraphQL(Storage storage)
	{
		// Setup caching of queries
		Cache<String, PreparsedDocumentEntry> cache = Caffeine.newBuilder()
			.maximumWeight(1024 * 10) // TODO: Configuration
			.weigher((String key, PreparsedDocumentEntry value) -> key.length())
			.build();

		PreparsedDocumentProvider queryProvider = (in, compute) -> cache.get(in.getQuery(), k -> compute.apply(in));

		// TODO: ApolloPersistedQuery support

		return new GraphQLGenerator(storage)
			.generate()
			.preparsedDocumentProvider(queryProvider)
			.build();
	}

	private static void logIssues(ListIterable<? extends ValidationMessage> m)
	{
		m.forEach(msg -> {
			switch(msg.getLevel())
			{
				case ERROR:
					logger.error(msg.format());
					break;
				case WARNING:
					logger.warn(msg.format());
					break;
				case INFO:
					logger.info(msg.format());
					break;
			}
		});
	}
}

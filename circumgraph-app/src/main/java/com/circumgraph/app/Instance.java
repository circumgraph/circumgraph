package com.circumgraph.app;

import java.nio.file.Files;
import java.nio.file.Path;

import com.circumgraph.app.config.ConfigConfig;
import com.circumgraph.app.config.HTTPServerConfig;
import com.circumgraph.app.config.InstanceConfig;
import com.circumgraph.app.config.StorageConfig;
import com.circumgraph.graphql.GraphQLAPISchema;
import com.circumgraph.graphql.GraphQLGenerator;
import com.circumgraph.model.InterfaceDef;
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

public class Instance
{
	private static final Logger logger = LoggerFactory.getLogger("com.circumgraph");

	private final Storage storage;
	private final Vertx vertx;

	public Instance(Storage storage, Vertx vertx)
	{
		this.storage = storage;
		this.vertx = vertx;
	}

	public void close()
	{
		vertx.close()
			.toCompletionStage()
			.toCompletableFuture()
			.join();

		storage.close();
	}

	public static Instance start(InstanceConfig config)
	{
		var model = loadModel(config.getConfig());
		var storage = openStorage(config.getStorage(), model);

		try
		{
			var graphQL = createGraphQL(storage);
			var vertx = startServer(config.getHttp().getServer(), graphQL);

			return new Instance(storage, vertx);
		}
		catch(Exception e)
		{
			// Close the storage
			storage.close();

			if(e instanceof RuntimeException r)
			{
				throw r;
			}

			throw new MaybeTemporaryException(e.getMessage(), e);
		}
	}

	/**
	 * Load the model from the given directory. This will recursively look for
	 * `.graphql` and `.gql` files in the directory and load them.
	 *
	 * @param dir
	 * @return
	 */
	private static Model loadModel(ConfigConfig config)
	{
		var dir = config.getDir();
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
				throw new UnrecoverableException("Config directory does not exist, no schema will be loaded");
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
			var entities = model.get(StorageSchema.ENTITY_NAME, InterfaceDef.class)
				.get()
				.getImplementors();

			if(entities.isEmpty())
			{
				throw new UnrecoverableException("No entities defined, can not start");
			}

			logger.info(entities.size() + " entities available:");
			for(var type : entities)
			{
				logger.info("  " + type.getName());
			}

			return model;
		}
		catch(UnrecoverableException e)
		{
			throw e;
		}
		catch(ModelValidationException e)
		{
			logIssues(e.getIssues());
			throw new UnrecoverableException("Errors during loading of model, can not start");
		}
		catch(Exception e)
		{
			logger.error("Failed to load model due to unexpected error; " + e.getMessage(), e);
			throw new UnrecoverableException("Errors during loading of model; " + e.getMessage());
		}
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

	private static Storage openStorage(StorageConfig config, Model model)
	{
		var dir = config.getDir();
		logger.info("Storing data in {}", dir.toAbsolutePath().normalize());

		if(! Files.isDirectory(dir))
		{
			throw new UnrecoverableException("Data directory does not exist, storage will not be opened");
		}

		if(! Files.isWritable(dir))
		{
			throw new UnrecoverableException("Data directory is not writable, storage will not be opened");
		}

		try
		{
			return Storage.open(model, dir)
				.start()
				.block();
		}
		catch(Exception e)
		{
			// Exception while starting, gracefully ask to exit
			throw new MaybeTemporaryException("Unexpected error opening storage; " + e.getMessage() ,e);
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

	private static Vertx startServer(
		HTTPServerConfig config,
		GraphQL graphQL
	)
	{
		var vertx = Vertx.vertx();

		var graphQLHandlerOptions = new GraphQLHandlerOptions()
			.setRequestBatchingEnabled(true);

		var graphQLHandler = GraphQLHandler.create(graphQL, graphQLHandlerOptions);

		Router router = Router.router(vertx);
		router.post().handler(BodyHandler.create());
		router.route("/graphql").handler(graphQLHandler);

		var port = config.getPort();
		vertx.createHttpServer()
			.requestHandler(router)
			.listen(port)
			.toCompletionStage()
			.toCompletableFuture()
			.join();


		logger.info("HTTP server started at {}", port);

		return vertx;
	}
}

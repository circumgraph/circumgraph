package com.circumgraph.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import com.circumgraph.graphql.GraphQLAPISchema;
import com.circumgraph.graphql.GraphQLGenerator;
import com.circumgraph.model.Model;
import com.circumgraph.schema.graphql.GraphQLSchema;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StorageSchema;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.GraphQL;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import io.quarkus.runtime.ShutdownEvent;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.ApolloWSHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;

/**
 * Logic that wires together all the modules into the public facing application.
 *
 * TODO: If either the config is invalid or data directory doesn't exist - hang / exit gracefully after a time out
 */
@ApplicationScoped
public class CircumgraphApp
{
	private static final Logger logger = LoggerFactory.getLogger("com.circumgraph");

	public void setupRouter(
		@Observes Router router,
		GraphQL graphQL
	)
	{
		router.post().handler(BodyHandler.create());
		router.route("/graphql").handler(ApolloWSHandler.create(graphQL));
		router.route("/graphql").handler(GraphQLHandler.create(graphQL));
	}

	@Singleton
	public Model model(
		@ConfigProperty(name="config.dir") Path dir
	)
		throws IOException
	{
		var matching = Lists.mutable.<Path>empty();

		logger.info("Loading config from {}", dir.toAbsolutePath().normalize());

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
		var entities = model.getImplements("Entity");
		logger.info(entities.size() + " entities available:");
		for(var type : entities)
		{
			logger.info("  " + type.getName());
		}

		return model;
	}

	/**
	 * Provide the {@link Storage} instance on top of the active {@link Model}.
	 *
	 * @param dir
	 * @param model
	 * @return
	 */
	@Singleton
	public Storage storage(
		@ConfigProperty(name="data.dir") Path dir,
		Model model
	)
	{
		logger.info("Storing data in {}", dir.toAbsolutePath().normalize());

		if(! Files.isDirectory(dir))
		{
			logger.warn("Data directory does not exist");
		}

		return Storage.open(model, dir)
			.start()
			.block();
	}

	/**
	 * Shutdown the storage when stopping.
	 *
	 * @param ev
	 * @param storage
	 */
	public void onStop(
		@Observes ShutdownEvent ev,
		Storage storage
	)
	{
		logger.info("Gracefully closing storage");

		storage.close();

		logger.info("Storage closed");
	}

	@Singleton
	public GraphQL graphql(
		Storage storage
	)
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
}

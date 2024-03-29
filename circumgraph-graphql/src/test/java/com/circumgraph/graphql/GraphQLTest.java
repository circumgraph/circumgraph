package com.circumgraph.graphql;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.circumgraph.model.Model;
import com.circumgraph.schema.graphql.GraphQLSchema;
import com.circumgraph.schema.graphql.TextSource;
import com.circumgraph.storage.Storage;
import com.circumgraph.storage.StorageSchema;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.GraphQLError;
import graphql.schema.idl.SchemaPrinter;

public class GraphQLTest
{
	@TempDir
	Path tmp;

	protected Storage storage;

	@AfterEach
	public void after()
		throws Exception
	{
		if(storage != null)
		{
			storage.close();
		}
	}

	protected Context open(String schema)
	{
		if(storage != null)
		{
			throw new AssertionError("Can not open multiple storages in the same test");
		}

		var model = Model.create()
			.addSchema(StorageSchema.INSTANCE)
			.addSchema(new GraphQLAPISchema())
			.addSchema(GraphQLSchema.create(TextSource.create("<test>", schema)))
			.build();

		storage = Storage.open(model, tmp)
			.start()
			.block();

		// Create a GraphQL instance for the storage
		var generator = new GraphQLGenerator(storage);
		var generatedSchema = generator.generateSchema();
		var ql = generator.generate(generatedSchema)
			.build();

		return new Context(storage, generatedSchema, ql);
	}

	public static class Context
	{
		private final Storage storage;
		private final graphql.schema.GraphQLSchema schema;
		private final GraphQL ql;

		public Context(
			Storage storage,
			graphql.schema.GraphQLSchema schema,
			GraphQL ql
		)
		{
			this.storage = storage;
			this.schema = schema;
			this.ql = ql;
		}

		public void printSchema()
		{
			System.out.println(new SchemaPrinter().print(schema));
		}

		public graphql.schema.GraphQLSchema schema()
		{
			return schema;
		}

		public Storage storage()
		{
			return storage;
		}

		public Result execute(String query)
		{
			ExecutionResult result = ql.execute(ExecutionInput.newExecutionInput(query)
				.context(GraphQLContext.newContext()
					.of("test", "TestEnv")
				)
				.build()
			);

			return new Result(result);
		}

		public Result execute(String query, Map<String, Object> variables)
		{
			ExecutionResult result = ql.execute(ExecutionInput.newExecutionInput(query)
				.variables(variables)
				.context(GraphQLContext.newContext()
					.of("test", "TestEnv")
				)
				.build()
			);

			return new Result(result);
		}
	}

	public static class Result
	{
		private final ExecutionResult result;

		public Result(ExecutionResult result)
		{
			this.result = result;
		}

		public void assertNoErrors()
		{
			if(! result.getErrors().isEmpty())
			{
				AssertionError error = new AssertionError("Expected no errors, got: " + result.getErrors().stream()
					.map(e -> e.getMessage())
					.collect(Collectors.joining(", "))
				);

				for(GraphQLError e : result.getErrors())
				{
					if(e instanceof ExceptionWhileDataFetching)
					{
						error.addSuppressed(((ExceptionWhileDataFetching) e).getException());
					}
					else if(e instanceof Throwable)
					{
						error.addSuppressed((Throwable) e);
					}
				}

				throw error;
			}
		}

		public void assertValidationError(String op, String path, String type)
		{
			for(var error : result.getErrors())
			{
				var extension = error.getExtensions();
				if("VALIDATION_ERROR".equals(extension.get("code")) && type.equals(extension.get("type")))
				{
					return;
				}
			}

			if(result.getErrors().isEmpty())
			{
				throw new AssertionError("Expected validation error of type " + type + ", but got no errors");
			}

			AssertionError error = new AssertionError("Expected validation errors of type " +  type + ", got: " + result.getErrors().stream()
				.map(e -> e.getMessage())
				.collect(Collectors.joining(", "))
			);

			for(GraphQLError e : result.getErrors())
				{
					if(e instanceof ExceptionWhileDataFetching)
					{
						error.addSuppressed(((ExceptionWhileDataFetching) e).getException());
					}
					else if(e instanceof Throwable)
					{
						error.addSuppressed((Throwable) e);
					}
				}

				throw error;
		}

		public ListIterable<GraphQLError> errors()
		{
			return Lists.immutable.ofAll(result.getErrors());
		}

		public <T> T getData()
		{
			return result.getData();
		}

		public <T> T pick(String... path)
		{
			return (T) pick((Map) result.getData(), path, 0);
		}

		private Object pick(Map<String, Object> data, String[] path, int index)
		{
			String part = path[index];
			Object partData = data.get(part);
			if(partData == null)
			{
				return null;
			}

			if(index == path.length - 1)
			{
				return partData;
			}

			if(partData instanceof List)
			{
				return pick((List) partData, path, index + 1);
			}
			else if(partData instanceof Map)
			{
				return pick((Map) partData, path, index + 1);
			}
			else
			{
				throw new AssertionError("Can not traverse down into object at key " + part + ": " + partData);
			}
		}

		private Object pick(List data, String[] path, int index)
		{
			String part = path[index];
			int parsedPart = Integer.parseInt(part);

			if(parsedPart >= data.size()) return null;
			Object partData = data.get(parsedPart);

			if(index == path.length - 1)
			{
				return partData;
			}
			else if(partData instanceof List)
			{
				return pick((List) partData, path, index + 1);
			}
			else if(partData instanceof Map)
			{
				return pick((Map) partData, path, index + 1);
			}
			else
			{
				throw new AssertionError("Can not traverse down into object at key " + part + ": " + partData);
			}
		}
	}
}

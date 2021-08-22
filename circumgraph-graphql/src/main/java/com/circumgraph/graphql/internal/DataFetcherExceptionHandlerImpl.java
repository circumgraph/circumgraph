package com.circumgraph.graphql.internal;

import java.util.List;
import java.util.concurrent.CompletionException;

import com.circumgraph.storage.StorageValidationException;

import org.eclipse.collections.api.factory.Maps;

import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;

/**
 * Handler that takes care of mapping Circumgraph specific exceptions into
 * GraphQL errors.
 */
public class DataFetcherExceptionHandlerImpl
	implements DataFetcherExceptionHandler
{
	@Override
	public DataFetcherExceptionHandlerResult onException(
		DataFetcherExceptionHandlerParameters handlerParameters
	)
	{
		var exception = handlerParameters.getException();
		if(exception instanceof CompletionException && exception.getCause() != null)
		{
			exception = exception.getCause();
		}

		List<GraphQLError> errors;
		if(exception instanceof StorageValidationException sve)
		{
			// Report a validation error
			errors = sve.getIssues().collect(msg -> GraphqlErrorBuilder.newError()
				.location(handlerParameters.getSourceLocation())
				.path(handlerParameters.getPath())
				.errorType(ErrorType.DataFetchingException)
				.message("Validation error: %s: %s", msg.getLocation().describe(), msg.getMessage())
				.extensions(Maps.mutable.of(
					"code", "VALIDATION_ERROR",
					"type", msg.getCode(),
					"arguments", msg.getArguments()
				))
				.build()
			).toList();
		}
		else
		{
			errors = List.of(
				new ExceptionWhileDataFetching(
					handlerParameters.getPath(),
					exception,
					handlerParameters.getSourceLocation()
				)
			);
		}

		return DataFetcherExceptionHandlerResult.newResult()
			.errors(errors)
			.build();
	}
}

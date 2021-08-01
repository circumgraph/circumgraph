package com.circumgraph.graphql.internal;

import com.circumgraph.storage.Storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ExecutionResult;
import graphql.execution.ExecutionContext;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import se.l4.silo.Transaction;

/**
 * {@link Instrumentation} that will create and automatically commit/rollback
 * {@link Transaction transactions} for GraphQL executions. This
 * instrumentation is also responsible for setting up the instance of
 * {@link StorageContext} available to {@link graphql.schema.DataFetcher}s.
 */
public class TransactionInstrumentation
	extends SimpleInstrumentation
{
	private final Logger log;

	public final Storage storage;

	public TransactionInstrumentation(Storage storage)
	{
		log = LoggerFactory.getLogger("com.circumgraph.graphql.transactions");
		this.storage = storage;
	}

	@Override
	public InstrumentationState createState()
	{
		return new State();
	}

	@Override
	public InstrumentationContext<ExecutionResult> beginExecution(
		InstrumentationExecutionParameters parameters
	)
	{
		var tx = storage.transactions().newTransaction().block();
		((State) parameters.getInstrumentationState()).tx = tx;
		log.debug("Initialized tx {}", tx);
		return new SimpleInstrumentationContext<ExecutionResult>()
		{
			@Override
			public void onCompleted(ExecutionResult result, Throwable t)
			{
				if(t != null || ! result.getErrors().isEmpty())
				{
					log.debug("Error occurred, rolling back tx {}", tx);
					tx.rollback().block();
				}
				else
				{
					log.debug("Committing tx {}", tx);
					tx.commit().block();
				}
			}
		};
	}

	@Override
	public ExecutionContext instrumentExecutionContext(
		ExecutionContext executionContext,
		InstrumentationExecutionParameters parameters
	)
	{
		var tx = ((State) parameters.getInstrumentationState()).tx;
		return executionContext.transform(builder -> {
			// TODO: Pull auth from the context

			builder.context(new StorageContext(tx));
		});
	}

	private class State
		implements InstrumentationState
	{
		private volatile Transaction tx;
	}
}

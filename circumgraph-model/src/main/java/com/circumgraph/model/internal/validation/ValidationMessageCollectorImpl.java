package com.circumgraph.model.internal.validation;

import java.util.Objects;
import java.util.function.Consumer;

import com.circumgraph.model.validation.SourceLocation;
import com.circumgraph.model.validation.ValidationBuilder;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageCollector;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

/**
 * Implementation of {@link ValidationMessageCollector}.
 */
public class ValidationMessageCollectorImpl
	implements ValidationMessageCollector
{
	private final Consumer<ValidationMessage> consumer;

	public ValidationMessageCollectorImpl(
		Consumer<ValidationMessage> consumer
	)
	{
		this.consumer = consumer;
	}

	@Override
	public ValidationBuilder add(ValidationMessageLevel level)
	{
		Objects.requireNonNull(level);

		return new ValidationBuilder()
		{
			private SourceLocation sourceLocation;
			private String message;
			private String code;
			private MutableMap<String, Object> arguments = Maps.mutable.empty();

			@Override
			public ValidationBuilder withLocation(SourceLocation location)
			{
				Objects.requireNonNull(location);

				this.sourceLocation = location;
				return this;
			}

			@Override
			public ValidationBuilder withMessage(String message, Object... args)
			{
				this.message = String.format(message, args);
				return this;
			}

			@Override
			public ValidationBuilder withCode(String code)
			{
				Objects.requireNonNull(code);
				this.code = code;
				return this;
			}

			@Override
			public ValidationBuilder withArgument(String key, Object value)
			{
				Objects.requireNonNull(key);
				arguments.put(key, value);
				return this;
			}

			@Override
			public void done()
			{
				Objects.requireNonNull(message, "human readable message required");
				Objects.requireNonNull(code, "code for message required");

				consumer.accept(new ValidationMessageImpl(
					SourceLocation.automatic(sourceLocation),
					level,
					message,
					code,
					arguments.toImmutable()
				));
			}
		};
	}
}

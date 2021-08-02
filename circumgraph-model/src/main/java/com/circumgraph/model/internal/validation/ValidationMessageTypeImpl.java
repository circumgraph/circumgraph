package com.circumgraph.model.internal.validation;

import java.util.Objects;

import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageLevel;
import com.circumgraph.model.validation.ValidationMessageType;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.SetIterable;

public class ValidationMessageTypeImpl
	implements ValidationMessageType
{
	private final ValidationMessageLevel level;
	private final String code;
	private final String message;
	private final ImmutableSet<String> arguments;

	private ValidationMessageTypeImpl(
		ValidationMessageLevel level,
		String code,
		String message,
		ImmutableSet<String> arguments
	)
	{
		this.level = level;
		this.code = code;
		this.message = message;
		this.arguments = arguments;
	}

	@Override
	public ValidationMessageLevel getLevel()
	{
		return level;
	}

	@Override
	public String getCode()
	{
		return code;
	}

	@Override
	public SetIterable<String> getArguments()
	{
		return arguments;
	}

	@Override
	public String format(MapIterable<String, Object> arguments)
	{
		String result = message;

		for(var e : arguments.keyValuesView())
		{
			result = result.replace("{{" + e.getOne() + "}}", String.valueOf(e.getTwo()));
		}

		return result;
	}

	@Override
	public ValidationMessage.Builder toMessage()
	{
		return ValidationMessage.create(this);
	}

	public static Builder create(ValidationMessageLevel level)
	{
		return new BuilderImpl(level, null, null, Sets.immutable.empty());
	}

	private static class BuilderImpl
		implements Builder
	{
		private final ValidationMessageLevel level;
		private final String code;
		private final String message;
		private final ImmutableSet<String> arguments;

		public BuilderImpl(
			ValidationMessageLevel level,
			String code,
			String message,
			ImmutableSet<String> arguments
		)
		{
			this.level = level;
			this.code = code;
			this.message = message;
			this.arguments = arguments;
		}

		@Override
		public Builder withMessage(String message)
		{
			return new BuilderImpl(
				level,
				code,
				message,
				arguments
			);
		}

		@Override
		public Builder withCode(String code)
		{
			Objects.requireNonNull(code);

			return new BuilderImpl(
				level,
				code,
				message,
				arguments
			);
		}

		@Override
		public Builder withArgument(String key)
		{
			Objects.requireNonNull(key);

			return new BuilderImpl(
				level,
				code,
				message,
				arguments.newWith(key)
			);
		}

		@Override
		public ValidationMessageType build()
		{
			Objects.requireNonNull(message, "human readable message required");
			Objects.requireNonNull(code, "code for message required");

			return new ValidationMessageTypeImpl(
				level,
				code,
				message,
				arguments.toImmutable()
			);
		}
	}
}

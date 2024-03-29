package com.circumgraph.model.internal.validation;

import java.util.Objects;

import com.circumgraph.model.HasLocation;
import com.circumgraph.model.Location;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageLevel;
import com.circumgraph.model.validation.ValidationMessageType;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MapIterable;

/**
 * Implementation of {@link ValidationMessage}.
 */
public class ValidationMessageImpl
	implements ValidationMessage
{
	private final Location location;
	private final ValidationMessageLevel level;
	private final String code;
	private final MapIterable<String, Object> arguments;
	private final String message;

	public ValidationMessageImpl(
		Location sourceLocation,
		ValidationMessageLevel level,
		String code,
		MapIterable<String, Object> arguments,
		String message
	)
	{
		this.location = sourceLocation;
		this.level = level;
		this.code = code;
		this.arguments = arguments;
		this.message = message;
	}

	@Override
	public Location getLocation()
	{
		return location;
	}

	@Override
	public ValidationMessageLevel getLevel()
	{
		return level;
	}

	@Override
	public String getMessage()
	{
		return message;
	}

	@Override
	public String getCode()
	{
		return code;
	}

	@Override
	public MapIterable<String, Object> getArguments()
	{
		return arguments;
	}

	@Override
	public String format()
	{
		return location.describe() + ": " + code + ": "	+ message;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(arguments, code, level, message, location);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		ValidationMessageImpl other = (ValidationMessageImpl) obj;
		return Objects.equals(arguments, other.arguments)
			&& Objects.equals(code, other.code)
			&& level == other.level
			&& Objects.equals(message, other.message)
			&& Objects.equals(location, other.location);
	}

	@Override
	public String toString()
	{
		return "ValidationMessageImpl{"
			+ "level=" + level
			+ ", message=" + message
			+ ", code=" + code
			+ ", arguments=" + arguments
			+ ", sourceLocation=" + location
			+ "}";
	}

	/**
	 * Create a builder for the given type.
	 *
	 * @param type
	 * @return
	 */
	public static Builder create(ValidationMessageType type)
	{
		Objects.requireNonNull(type, "type is required");

		return new BuilderImpl(
			type,
			null,
			Maps.immutable.empty()
		);
	}

	private static class BuilderImpl
		implements Builder
	{
		private final ValidationMessageType type;
		private final Location location;
		private final ImmutableMap<String, Object> arguments;

		public BuilderImpl(
			ValidationMessageType type,
			Location location,
			ImmutableMap<String, Object> arguments
		)
		{
			this.type = type;
			this.location = location;
			this.arguments = arguments;
		}

		@Override
		public Builder withLocation(Location sourceLocation)
		{
			Objects.requireNonNull(sourceLocation);

			return new BuilderImpl(
				type,
				sourceLocation,
				arguments
			);
		}

		@Override
		public Builder withLocation(HasLocation object)
		{
			return withLocation(object.getDefinedAt());
		}

		@Override
		public Builder withArgument(String key, Object value)
		{
			Objects.requireNonNull(key);

			return new BuilderImpl(
				type,
				location,
				arguments.newWithKeyValue(key, value)
			);
		}

		@Override
		public ValidationMessage build()
		{
			Objects.requireNonNull(location, "location is required");

			return new ValidationMessageImpl(
				location,
				type.getLevel(),
				type.getCode(),
				arguments.toImmutable(),
				type.format(arguments)
			);
		}
	}
}

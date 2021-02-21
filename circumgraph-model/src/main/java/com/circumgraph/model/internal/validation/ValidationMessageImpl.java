package com.circumgraph.model.internal.validation;

import java.util.Objects;

import com.circumgraph.model.validation.SourceLocation;
import com.circumgraph.model.validation.ValidationMessage;
import com.circumgraph.model.validation.ValidationMessageLevel;

import org.eclipse.collections.api.map.MapIterable;

/**
 * Implementation of {@link ValidationMessage}.
 */
public class ValidationMessageImpl
	implements ValidationMessage
{
	private final SourceLocation sourceLocation;
	private final ValidationMessageLevel level;
	private final String message;
	private final String code;
	private final MapIterable<String, Object> arguments;

	public ValidationMessageImpl(
		SourceLocation sourceLocation,
		ValidationMessageLevel level,
		String message,
		String code,
		MapIterable<String, Object> arguments
	)
	{
		this.sourceLocation = sourceLocation;
		this.level = level;
		this.message = message;
		this.code = code;
		this.arguments = arguments;
	}

	@Override
	public SourceLocation getLocation()
	{
		return sourceLocation;
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
	public int hashCode()
	{
		return Objects.hash(arguments, code, level, message, sourceLocation);
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
			&& Objects.equals(sourceLocation, other.sourceLocation);
	}

	@Override
	public String toString()
	{
		return "ValidationMessageImpl{"
			+ "level=" + level
			+ ", message=" + message
			+ ", code=" + code
			+ ", arguments=" + arguments
			+ ", sourceLocation=" + sourceLocation
			+ "}";
	}


}

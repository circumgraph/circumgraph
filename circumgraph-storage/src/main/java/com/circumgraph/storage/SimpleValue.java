package com.circumgraph.storage;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.storage.internal.SimpleValueImpl;

public interface SimpleValue
	extends Value
{
	/**
	 * Get the raw value.
	 *
	 * @return
	 */
	Object get();

	/**
	 * Get this value as a {@link String}.
	 *
	 * @return
	 */
	String asString();

	/**
	 * Get this value as an int.
	 *
	 * @return
	 */
	int asInt();

	/**
	 * Get this value as a float.
	 *
	 * @return
	 */
	double asFloat();

	/**
	 * Get this value as a boolean.
	 *
	 * @return
	 */
	boolean asBoolean();

	/**
	 * Get this value as an ID.
	 *
	 * @return
	 */
	long asID();

	/**
	 * Get the value as the given type.
	 *
	 * @param <T>
	 * @param type
	 * @return
	 */
	<T> T as(Class<T> type);

	@Override
	SimpleValueDef getDefinition();

	/**
	 * Create a value for the given definition.
	 *
	 * @param def
	 * @param value
	 * @return
	 */
	static SimpleValue create(SimpleValueDef def, Object value)
	{
		return new SimpleValueImpl(def, value);
	}

	static SimpleValue createString(String value)
	{
		return create(ScalarDef.STRING, value);
	}

	static SimpleValue createBoolean(boolean value)
	{
		return create(ScalarDef.BOOLEAN, value);
	}

	static SimpleValue createFloat(double value)
	{
		return create(ScalarDef.FLOAT, value);
	}

	static SimpleValue createInt(int value)
	{
		return create(ScalarDef.INT, value);
	}
}

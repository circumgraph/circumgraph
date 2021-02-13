package com.circumgraph.values;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.model.SimpleValueDef;
import com.circumgraph.values.internal.SimpleValueImpl;

public interface SimpleValue
	extends Value
{
	/**
	 * Get the raw value.
	 *
	 * @return
	 */
	Object get();

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

	static SimpleValue createInt(long value)
	{
		return create(ScalarDef.INT, value);
	}
}

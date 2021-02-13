package com.circumgraph.model;

import com.circumgraph.model.internal.ScalarDefImpl;

/**
 * Scalar representation.
 */
public interface ScalarDef
	extends SimpleValueDef
{
	/**
	 * String scalar.
	 */
	static final ScalarDef STRING = new ScalarDefImpl("String", null);

	/**
	 * Int scalar.
	 */
	static final ScalarDef INT = new ScalarDefImpl("Int", null);

	/**
	 * Float scalar.
	 */
	static final ScalarDef FLOAT = new ScalarDefImpl("Float", null);

	/**
	 * Boolean scalar.
	 */
	static final ScalarDef BOOLEAN = new ScalarDefImpl("Boolean", null);

	/**
	 * ID scalar.
	 */
	static final ScalarDef ID = new ScalarDefImpl("ID", null);
}

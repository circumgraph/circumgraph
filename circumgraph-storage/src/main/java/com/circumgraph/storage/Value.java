package com.circumgraph.storage;

import com.circumgraph.model.TypeDef;

public interface Value
{
	/**
	 * Get the definition of this data.
	 *
	 * @return
	 */
	TypeDef getDefinition();
}

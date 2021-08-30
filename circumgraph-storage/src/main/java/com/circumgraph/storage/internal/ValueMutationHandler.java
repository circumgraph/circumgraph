package com.circumgraph.storage.internal;

import com.circumgraph.model.OutputTypeDef;
import com.circumgraph.storage.Value;
import com.circumgraph.storage.mutation.Mutation;
import com.circumgraph.storage.types.ValueMapper;
import com.circumgraph.storage.types.ValueProvider;
import com.circumgraph.storage.types.ValueValidator;

/**
 * Handler for a specific location of value, such as fields and items in a list.
 */
public interface ValueMutationHandler<V extends Value, M extends Mutation>
{
	/**
	 * Get the type handled.
	 *
	 * @return
	 */
	OutputTypeDef getDef();

	/**
	 * Get provider used for default values.
	 */
	ValueProvider<V> getDefault();

	/**
	 * Get the mapper for this value.
	 *
	 * @return
	 */
	ValueMapper<V, M> getMapper();

	/**
	 * Get the validator in use for this value.
	 *
	 * @return
	 */
	ValueValidator<V> getValidator();
}

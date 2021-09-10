package com.circumgraph.storage.scalars;

import java.util.Optional;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.internal.ScalarsImpl;

import org.eclipse.collections.api.RichIterable;

/**
 * Access {@link Scalar} instances.
 */
public interface Scalars
{
	/**
	 * Get all of the scalars that are defined.
	 *
	 * @return
	 */
	RichIterable<? extends Scalar<?, ?>> list();

	/**
	 * Get the {@link Scalar} implementation for the given type.
	 *
	 * @param def
	 *   type to get scalar for
	 * @return
	 *   optional with scalar
	 */
	Optional<Scalar<?, ?>> get(ScalarDef def);

	/**
	 * Get the activate instance of {@link Scalars}.
	 *
	 * @return
	 */
	static Scalars instance()
	{
		return ScalarsImpl.INSTANCE;
	}
}

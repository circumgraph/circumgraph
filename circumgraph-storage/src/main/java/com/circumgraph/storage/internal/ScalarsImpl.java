package com.circumgraph.storage.internal;

import java.util.Optional;

import com.circumgraph.model.ScalarDef;
import com.circumgraph.storage.scalars.BooleanScalar;
import com.circumgraph.storage.scalars.DurationScalar;
import com.circumgraph.storage.scalars.FloatScalar;
import com.circumgraph.storage.scalars.IDScalar;
import com.circumgraph.storage.scalars.IntScalar;
import com.circumgraph.storage.scalars.LocalDateScalar;
import com.circumgraph.storage.scalars.LocalDateTimeScalar;
import com.circumgraph.storage.scalars.LocalTimeScalar;
import com.circumgraph.storage.scalars.OffsetDateTimeScalar;
import com.circumgraph.storage.scalars.OffsetTimeScalar;
import com.circumgraph.storage.scalars.Scalar;
import com.circumgraph.storage.scalars.Scalars;
import com.circumgraph.storage.scalars.StringScalar;
import com.circumgraph.storage.scalars.ZonedDateTimeScalar;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.map.ImmutableMap;

/**
 * Implementation of {@link Scalars}.
 */
public class ScalarsImpl
	implements Scalars
{
	public static final ScalarsImpl INSTANCE = new ScalarsImpl();

	private final ImmutableMap<String, Scalar<?, ?>> instances;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ScalarsImpl()
	{
		var instances = Lists.immutable.<Scalar<?, ?>>of(
			new BooleanScalar(),
			new DurationScalar(),
			new FloatScalar(),
			new IDScalar(),
			new IntScalar(),
			new LocalDateScalar(),
			new LocalDateTimeScalar(),
			new LocalTimeScalar(),
			new OffsetDateTimeScalar(),
			new OffsetTimeScalar(),
			new StringScalar(),
			new ZonedDateTimeScalar()
		);

		this.instances = (ImmutableMap) instances
			.toMap(i -> i.getModelType().getName(), i -> i)
			.toImmutable();
	}

	@Override
	public RichIterable<? extends Scalar<?, ?>> list()
	{
		return instances;
	}

	@Override
	public Optional<Scalar<?, ?>> get(ScalarDef def)
	{
		return Optional.ofNullable(instances.get(def.getName()));
	}
}

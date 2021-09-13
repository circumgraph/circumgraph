package com.circumgraph.storage;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.Location;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.Model;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.types.ValueIndexer;
import com.circumgraph.storage.types.ValueProvider;

/**
 * Utilities for accessing storage specific enhancements to a {@link Model}
 * and its types.
 */
public class StorageModel
{
	public enum FieldType
	{
		/**
		 * Stored field, persisted in the storage.
		 */
		STORED,

		/**
		 * Dynamic field, field is calculated on the fly in the GraphQL layer
		 * above the storage.
		 */
		DYNAMIC
	}

	/**
	 * Location used by the enhancements added by the Storage model.
	 */
	public static final Location LOCATION = Location.create("Storage");

	/**
	 * Key for the type a {@link FieldDef} is. Set this to override a field
	 * being stored.
	 */
	public static final MetadataKey<FieldType> FIELD_TYPE = MetadataKey.create("storage:field-type", FieldType.class);

	public static final MetadataKey<ValueIndexer> FIELD_INDEXER = MetadataKey.create("storage:field-indexer", ValueIndexer.class);
	public static final MetadataKey<Boolean> FIELD_INDEXED = MetadataKey.create("storage:field-indexed", Boolean.class);
	private static final MetadataKey<Boolean> FIELD_SORTABLE = MetadataKey.create("storage:field-sortable", Boolean.class);
	private static final MetadataKey<Boolean> FIELD_HIGHLIGHTABLE = MetadataKey.create("storage:field-highlightable", Boolean.class);

	/**
	 * Key controlling a {@link ValueProvider} used for {@link FieldDef}.
	 */
	public static final MetadataKey<ValueProvider> FIELD_DEFAULT_VALUE_PROVIDER = MetadataKey.create("storage:field-default-value", ValueProvider.class);

	private StorageModel()
	{
	}

	/**
	 * Get the type that represents the actual entity for the given
	 * {@link StructuredDef}.
	 *
	 * @param def
	 * @return
	 */
	public static Optional<? extends StructuredDef> getEntity(StructuredDef def)
	{
		if(def.hasImplements(StorageSchema.ENTITY_NAME))
		{
			return Optional.of(def);
		}

		return def.findImplements(interfaceDef -> interfaceDef.hasImplements(StorageSchema.ENTITY_NAME));
	}

	/**
	 * Get if the given field is stored or not.
	 *
	 * @param field
	 * @return
	 */
	public static FieldType getFieldType(FieldDef field)
	{
		return field.getMetadata(FIELD_TYPE)
			.orElseGet(() -> {
				if(field.getArguments().isEmpty())
				{
					return FieldType.STORED;
				}

				return FieldType.DYNAMIC;
			});
	}

	/**
	 * Set the type of the given field.
	 *
	 * @param field
	 * @param type
	 */
	public static void setType(FieldDef field, FieldType type)
	{
		field.setRuntimeMetadata(FIELD_TYPE, type);
	}

	/**
	 * Get if this field is indexed.
	 *
	 * @param field
	 *   field
	 * @return
	 *   {@code true} if field is indexed
	 */
	public static boolean isIndexed(FieldDef field)
	{
		return field.getMetadata(FIELD_INDEXED).orElse(false);
	}

	/**
	 * Set if the field should be indexed.
	 *
	 * @param field
	 *   field that should have indexed value set
	 * @param v
	 *   {@code true} if field is indexed
	 */
	public static void setIndexed(FieldDef field, boolean v)
	{
		field.setRuntimeMetadata(FIELD_INDEXED, v);
	}

	/**
	 * Get the identifier of the indexer used for the given field.
	 *
	 * @param field
	 *   field to get indexer for
	 * @return
	 *   optional containing the indexer if the field is indexed, empty optional
	 *   otherwise
	 */
	public static Optional<ValueIndexer> getIndexer(FieldDef field)
	{
		return field.getMetadata(FIELD_INDEXER);
	}

	/**
	 * Set the type of indexer used for a given field.
	 *
	 * @param field
	 * @param type
	 */
	public static void setIndexer(FieldDef field, ValueIndexer type)
	{
		setIndexed(field, true);
		field.setRuntimeMetadata(FIELD_INDEXER, type);
	}

	/**
	 * Get if a certain field is sortable.
	 *
	 * @param field
	 * @return
	 */
	public static boolean isSortable(FieldDef field)
	{
		return field.getMetadata(FIELD_SORTABLE).orElse(false);
	}

	/**
	 * Set if a certain field is sortable.
	 *
	 * @param field
	 * @param sortable
	 */
	public static void setSortable(FieldDef field, boolean sortable)
	{
		field.setRuntimeMetadata(FIELD_SORTABLE, sortable);
	}

	/**
	 * Get if a certain field supports highlighting.
	 *
	 * @param field
	 * @return
	 */
	public static boolean isHighlightable(FieldDef field)
	{
		return field.getMetadata(FIELD_HIGHLIGHTABLE).orElse(false);
	}

	/**
	 * Set if a certain field should support highlighting.
	 *
	 * @param field
	 * @param sortable
	 */
	public static void setHighlightable(FieldDef field, boolean sortable)
	{
		field.setRuntimeMetadata(FIELD_HIGHLIGHTABLE, sortable);
	}

	/**
	 * Get the default provider for the given field.
	 *
	 * @param field
	 * @return
	 */
	public static Optional<ValueProvider> getDefaultProvider(FieldDef field)
	{
		return field.getMetadata(FIELD_DEFAULT_VALUE_PROVIDER);
	}

	/**
	 * Set the default provider for the given field.
	 *
	 * @param field
	 * @param provider
	 */
	public static void setDefaultProvider(FieldDef field, ValueProvider provider)
	{
		field.setRuntimeMetadata(FIELD_DEFAULT_VALUE_PROVIDER, provider);
	}
}

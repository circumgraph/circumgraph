package com.circumgraph.storage;

import java.util.Optional;

import com.circumgraph.model.FieldDef;
import com.circumgraph.model.MetadataKey;
import com.circumgraph.model.Model;
import com.circumgraph.model.StructuredDef;
import com.circumgraph.storage.types.ValueIndexer;

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

	private static MetadataKey<FieldType> FIELD_TYPE = MetadataKey.create("storage:field-type", FieldType.class);
	private static MetadataKey<ValueIndexer> FIELD_INDEXER = MetadataKey.create("storage:field-indexer", ValueIndexer.class);
	private static MetadataKey<Boolean> FIELD_INDEXED = MetadataKey.create("storage:field-indexed", Boolean.class);
	private static MetadataKey<Boolean> FIELD_SORTABLE = MetadataKey.create("storage:field-sortable", Boolean.class);
	private static MetadataKey<Boolean> FIELD_HIGHLIGHTABLE = MetadataKey.create("storage:field-highlightable", Boolean.class);

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
		field.setMetadata(FIELD_TYPE, type);
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
		field.setMetadata(FIELD_INDEXED, v);
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
		field.setMetadata(FIELD_INDEXER, type);
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
		field.setMetadata(FIELD_SORTABLE, sortable);
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
		field.setMetadata(FIELD_HIGHLIGHTABLE, sortable);
	}
}

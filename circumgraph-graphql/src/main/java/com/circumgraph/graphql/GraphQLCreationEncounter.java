package com.circumgraph.graphql;

import com.circumgraph.storage.Storage;

public interface GraphQLCreationEncounter
{
	/**
	 * Access the storage that is being generated.
	 *
	 * @return
	 */
	Storage getStorage();
}

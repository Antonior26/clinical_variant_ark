package org.gel.cva.storage.core.curatedvariant.adaptors;

import org.opencb.biodata.models.variant.CuratedVariant;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by priesgo on 07/01/17.
 */
//TODO: extend Iterable<CuratedVariant>
public interface CuratedVariantDBAdaptor extends AutoCloseable {

    /**
     * This method inserts a single CuratedVariant in the database. If the variant already exists... throw error?
     *
     * @param curatedVariant  List of curated variants in OpenCB data model to be inserted
     * @param options   Query modifiers, accepted values are: include, exclude, limit, skip, sort and count
     * @return A QueryResult with the number of inserted variants
     */
    QueryResult insert(CuratedVariant curatedVariant, QueryOptions options);

    /**
     * This method inserts CuratedVariants in the database. If the variant already exists... throw error?
     *
     * @param curatedVariants  List of curated variants in OpenCB data model to be inserted
     * @param options   Query modifiers, accepted values are: include, exclude, limit, skip, sort and count
     * @return A QueryResult with the number of inserted variants
     */
    QueryResult insert(List<CuratedVariant> curatedVariants, QueryOptions options);

}

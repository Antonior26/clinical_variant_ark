package org.gel.cva.storage.core.knownvariant.adaptors;

import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaArgumentException;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by priesgo on 07/01/17.
 */
//TODO: extend Iterable<CuratedVariant>
public interface KnownVariantDBAdaptor extends AutoCloseable {


    /**
     * Returns the number of documents in the known variants collection.
     * @return
     */
    Long count();

    /**
     * This method inserts a single KnownVariant in the database. If the variant already exists... throw error?
     * @param knownVariant      List of curated variants in OpenCB data model to be inserted
     * @param options           Query modifiers, accepted values are: include, exclude, limit, skip, sort and count
     * @return                  The known variant _id
     */
    String insert(KnownVariantWrapper knownVariant, QueryOptions options);

    /**
     * This method inserts a list of KnownVariants in the database. If the variant already exists... throw error?
     * @param knownVariants     List of curated variants in OpenCB data model to be inserted
     * @param options           Query modifiers, accepted values are: include, exclude, limit, skip, sort and count
     * @return                  The list of known variant _id
     */
    List<String> insert(List<KnownVariantWrapper> knownVariants, QueryOptions options);

    /**
     * Retrieves a KnownVariant by the basic variant attributes.
     * Normalization is applied to these attributes, so searching for chr19 and 19 returns the same results.
     * Also redundant base trimming and left alignment is applied.
     * @param chromosome        The chromosome
     * @param position          The position
     * @param reference         The reference base/s
     * @param alternate         The alternate base/s
     * @return                  The known variant found if any
     * @throws CvaException
     */
    KnownVariantWrapper find(String chromosome, Integer position, String reference, String alternate)
            throws CvaException;

    /**
     * Updates a known variant and returns a flag indicating if the update was correct.
     * @param knownVariantWrapper       The entity to update in the known variants collection
     * @return                          Boolean indicating if the update was correct
     * @throws CvaException
     */
    Boolean update(KnownVariantWrapper knownVariantWrapper) throws CvaException;
}

/*
 * Copyright 2017 Genomics England Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gel.cva.storage.mongodb.knownvariant.adaptors;


import com.mongodb.ErrorCategory;
import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteError;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaArgumentException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.IllegalCvaCredentialsException;
import org.gel.cva.storage.mongodb.knownvariant.converters.DocumentToKnownVariantConverter;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.ConsistencyStatus;
import org.gel.models.cva.avro.CurationClassification;
import org.gel.models.cva.avro.ManualCurationConfidence;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.commons.io.DataWriter;
import org.gel.cva.storage.core.knownvariant.adaptors.KnownVariantDBAdaptor;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static org.opencb.opencga.storage.mongodb.variant.adaptors.VariantMongoDBAdaptor.NUMBER_INSTANCES;

/**
 * @author Ignacio Medina <igmecas@gmail.com>
 * @author Jacobo Coll <jacobo167@gmail.com>
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class KnownVariantMongoDBAdaptor implements KnownVariantDBAdaptor {

    private boolean closeConnection;
    private MongoDataStoreManager mongoManager;
    private String collectionName;
    private MongoCollection<Document> knownVariantsCollection;
    private MongoCredentials credentials;
    private DocumentToKnownVariantConverter knownVariantConverter = new DocumentToKnownVariantConverter();
    protected static Logger logger = LoggerFactory.getLogger(KnownVariantMongoDBAdaptor.class);

    /**
     * Constructor using the configuration object
     * @param cvaConfiguration
     * @throws IllegalCvaConfigurationException
     * @throws IllegalCvaCredentialsException
     */
    public KnownVariantMongoDBAdaptor(CvaConfiguration cvaConfiguration)
            throws IllegalCvaConfigurationException, IllegalCvaCredentialsException {
        // Gets mongo credentials
        this.credentials = CvaConfiguration.getMongoCredentials();
        this.closeConnection = false;
        this.mongoManager = new MongoDataStoreManager(this.credentials.getDataStoreServerAddresses());
        this.collectionName = cvaConfiguration.getDefaultStorageEngine().getOptions().get("collection.knownvariants");
        //this.knownVariantsCollection = db.getCollection(collectionName);
        NUMBER_INSTANCES.incrementAndGet();

        MongoClient mongoClient = CvaConfiguration.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase(credentials.getMongoDbName());
        this.knownVariantsCollection = database.getCollection(this.collectionName);
    }

    @Override
    public Long count() {
        return this.knownVariantsCollection.count();
    }

    /**
     * This method inserts a single KnownVariant in the database. If the variant already exists... throw error?
     * @param knownVariant      List of curated variants in OpenCB data model to be inserted
     * @param options           Query modifiers, accepted values are: include, exclude, limit, skip, sort and count
     * @return                  The known variant _id
     */
    @Override
    public String insert(KnownVariantWrapper knownVariant, QueryOptions options) {

        // Creates a set of converters
        Document curatedVariantDocument = this.knownVariantConverter.convertToStorageType(knownVariant);
        try {
            this.knownVariantsCollection.insertOne(curatedVariantDocument);
        } catch (MongoWriteException ex) {
            if (ex.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                //TODO: log this problem and notify somehow in the output
            }
            else {
                throw ex;
            }
        }
        return (String) curatedVariantDocument.get("_id");
    }

    /**
     * This method inserts a list of KnownVariants in the database. If the variant already exists... throw error?
     * @param knownVariants     List of curated variants in OpenCB data model to be inserted
     * @param options           Query modifiers, accepted values are: include, exclude, limit, skip, sort and count
     * @return                  The list of known variant _id
     */
    @Override
    public List<String> insert(List<KnownVariantWrapper> knownVariants, QueryOptions options) {
        //TODO: implement the insertion in batches of variants
        List<String> results = new LinkedList<>();
        for (KnownVariantWrapper knownVariantWrapper : knownVariants) {
            String id = this.insert(knownVariantWrapper, options);
            results.add(id);
        }
        return results;
    }

    /**
     * Retrieves a KnownVariant by the basic variant attributes.
     * Normalization is applied to these attributes, so searching for chr19 and 19 returns the same results.
     * Also redundant base trimming and left alignment is applied.
     * @param chromosome        The chromosome
     * @param position          The position
     * @param reference         The reference base/s
     * @param alternate         The alternate base/s
     * @return                  The known variant found if any, otherwise returns null
     * @throws CvaException
     */
    public KnownVariantWrapper find(String chromosome, Integer position, String reference, String alternate)
            throws CvaException {

        // Creates a new variant and serializes to Bson for two reasons:
        //  * get the variant id to search the database
        //  * consider variant normalization in search
        List<KnownVariantWrapper> variantsToSearch = null;
        try {
            variantsToSearch = KnownVariantWrapper.buildKnownVariant(
                    "find",
                    chromosome,
                    position,
                    reference,
                    alternate,
                    true
            );
        }
        catch (VariantAnnotatorException ex) {
            // this exception will be never thrown as we are not annotating
        }
        if (variantsToSearch.size() > 1) {
            throw new CvaException("Multi-allelic variants are not supported in search!");
        }
        KnownVariantWrapper variantToSearch = variantsToSearch.get(0);
        Document variantToSearchDoc = this.knownVariantConverter.convertToStorageType(variantToSearch);
        String variantId = (String) variantToSearchDoc.get("_id");
        // Search in MongoDB
        Document foundDocument = this.knownVariantsCollection.find(eq("_id", variantId)).first();
        KnownVariantWrapper foundKnownVariant = null;
        if (foundDocument != null) {
            foundKnownVariant = this.knownVariantConverter.convertToDataModelType(foundDocument);
        }
        return foundKnownVariant;
    }

    /**
     * Updates a known variant and returns a flag indicating if the update was correct.
     * @param knownVariantWrapper       The entity to update in the known variants collection
     * @return                          Boolean indicating if the update was correct
     * @throws CvaException
     */
    @Override
    public Boolean update(KnownVariantWrapper knownVariantWrapper)
            throws CvaException {

        // Updates the database
        Document knownVariantWrapperDoc = this.knownVariantConverter.convertToStorageType(knownVariantWrapper);
        UpdateResult updateResult = this.knownVariantsCollection.updateOne(eq("_id", (String)knownVariantWrapperDoc.get("_id")),
                new Document("$set", knownVariantWrapperDoc));
        return updateResult.getModifiedCount() == 1;
    }

    @Override
    public void close() throws IOException {
        if (closeConnection) {
            mongoManager.close();
        }
        NUMBER_INSTANCES.decrementAndGet();
    }

    //TODO: implement Iterable interface
    /*
    @Override
    public CuratedVariantDBIterator iterator() {
        return iterator(new Query(), new QueryOptions());
    }

    //@Override
    public CuratedVariantDBIterator iterator(Query query, QueryOptions options) {
        if (options == null) {
            options = new QueryOptions();
        }
        if (query == null) {
            query = new Query();
        }
        Document mongoQuery = parseQuery(query);
        Document projection = createProjection(query, options);
        DocumentToVariantConverter converter = getDocumentToVariantConverter(query, options);
        options.putIfAbsent(MongoDBCollection.BATCH_SIZE, 100);

        // Short unsorted queries with timeout or limit don't need the persistent cursor.
        if (options.containsKey(QueryOptions.TIMEOUT)
                || options.containsKey(QueryOptions.LIMIT)
                || !options.containsKey(QueryOptions.SORT)) {
            FindIterable<Document> dbCursor = knownVariantsCollection.nativeQuery().find(mongoQuery, projection, options);
            return new VariantMongoDBIterator(dbCursor, converter);
        } else {
            return VariantMongoDBIterator.persistentIterator(knownVariantsCollection, mongoQuery, projection, options, converter);
        }
    }

    @Override
    public void forEach(Consumer<? super CuratedVariant> action) {
        forEach(new Query(), action, new QueryOptions());
    }

    //@Override
    public void forEach(Query query, Consumer<? super CuratedVariant> action, QueryOptions options) {
        Objects.requireNonNull(action);
        VariantDBIterator variantDBIterator = iterator(query, options);
        while (variantDBIterator.hasNext()) {
            action.accept(variantDBIterator.next());
        }
    }
    */

}

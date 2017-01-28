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


import org.bson.Document;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.IllegalCvaCredentialsException;
import org.gel.cva.storage.mongodb.knownvariant.converters.DocumentToKnownVariantConverter;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariant;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.commons.io.DataWriter;
import org.gel.cva.storage.core.knownvariant.adaptors.KnownVariantDBAdaptor;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.*;

import static org.opencb.opencga.storage.mongodb.variant.adaptors.VariantMongoDBAdaptor.NUMBER_INSTANCES;

/**
 * @author Ignacio Medina <igmecas@gmail.com>
 * @author Jacobo Coll <jacobo167@gmail.com>
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class KnownVariantMongoDBAdaptor implements KnownVariantDBAdaptor {

    private boolean closeConnection;
    private MongoDataStoreManager mongoManager;
    private MongoDataStore db;
    private String collectionName;
    private MongoDBCollection curatedVariantsCollection;
    private MongoCredentials credentials;

    @Deprecated
    private DataWriter dataWriter;

    protected static Logger logger = LoggerFactory.getLogger(KnownVariantMongoDBAdaptor.class);

    public KnownVariantMongoDBAdaptor(CvaConfiguration cvaConfiguration)
            throws IllegalCvaConfigurationException, IllegalCvaCredentialsException {
        // Gets mongo credentials
        this.credentials = CvaConfiguration.getMongoCredentials();
        this.closeConnection = false;
        this.mongoManager = new MongoDataStoreManager(this.credentials.getDataStoreServerAddresses());
        this.db = mongoManager.get(credentials.getMongoDbName(), credentials.getMongoDBConfiguration());
        this.collectionName = cvaConfiguration.getStorageEngines().get(0).getOptions().get("collection.knownvariants");
        this.curatedVariantsCollection = db.getCollection(collectionName);
        NUMBER_INSTANCES.incrementAndGet();
    }

    @Override
    public QueryResult insert(KnownVariant curatedVariant, QueryOptions options) {
        // Creates a set of converters
        DocumentToKnownVariantConverter curatedVariantConverter = new DocumentToKnownVariantConverter();
        Document curatedVariantDocument = curatedVariantConverter.convertToStorageType(curatedVariant);
        QueryResult result = this.curatedVariantsCollection.insert(curatedVariantDocument, options);

        return result;
    }

    @Override
    public QueryResult insert(List<KnownVariant> curatedVariants, QueryOptions options) {
        //TODO: implement the insertion in batches of variants
        throw new NotImplementedException();
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
            FindIterable<Document> dbCursor = curatedVariantsCollection.nativeQuery().find(mongoQuery, projection, options);
            return new VariantMongoDBIterator(dbCursor, converter);
        } else {
            return VariantMongoDBIterator.persistentIterator(curatedVariantsCollection, mongoQuery, projection, options, converter);
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

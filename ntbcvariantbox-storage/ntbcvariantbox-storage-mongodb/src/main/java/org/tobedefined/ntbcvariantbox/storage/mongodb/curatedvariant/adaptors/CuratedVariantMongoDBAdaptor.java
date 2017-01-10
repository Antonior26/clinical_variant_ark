/*
 * Copyright 2015-2016 OpenCB
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

package org.tobedefined.ntbcvariantbox.storage.mongodb.curatedvariant.adaptors;


import org.bson.Document;
import org.opencb.biodata.models.variant.CuratedVariant;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.commons.io.DataWriter;
import org.tobedefined.ntbcvariantbox.storage.core.curatedvariant.adaptors.CuratedVariantDBAdaptor;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;
import org.opencb.opencga.storage.mongodb.variant.converters.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobedefined.ntbcvariantbox.storage.mongodb.curatedvariant.converters.DocumentToCuratedVariantConverter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ignacio Medina <igmecas@gmail.com>
 * @author Jacobo Coll <jacobo167@gmail.com>
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class CuratedVariantMongoDBAdaptor implements CuratedVariantDBAdaptor {

    private boolean closeConnection;
    private final MongoDataStoreManager mongoManager;
    private final MongoDataStore db;
    private final String collectionName;
    private final MongoDBCollection curatedVariantsCollection;
    private final MongoCredentials credentials;

    @Deprecated
    private DataWriter dataWriter;

    protected static Logger logger = LoggerFactory.getLogger(CuratedVariantMongoDBAdaptor.class);

    // Number of opened dbAdaptors
    public static final AtomicInteger NUMBER_INSTANCES = new AtomicInteger(0);


    public CuratedVariantMongoDBAdaptor(MongoCredentials credentials, String curatedVariantsCollectionName)
            throws UnknownHostException {
        this(new MongoDataStoreManager(
                credentials.getDataStoreServerAddresses()), credentials, curatedVariantsCollectionName);
        this.closeConnection = true;
    }


    public CuratedVariantMongoDBAdaptor(MongoDataStoreManager mongoManager, MongoCredentials credentials,
                                 String curatedVariantsCollectionName)
            throws UnknownHostException {
        // MongoDB configuration
        this.closeConnection = false;
        this.credentials = credentials;
        this.mongoManager = mongoManager;
        this.db = mongoManager.get(credentials.getMongoDbName(), credentials.getMongoDBConfiguration());
        this.collectionName = curatedVariantsCollectionName;
        this.curatedVariantsCollection = db.getCollection(collectionName);
        NUMBER_INSTANCES.incrementAndGet();
    }


    @Override
    public QueryResult insert(CuratedVariant curatedVariant, QueryOptions options) {
        // Creates a set of converters
        DocumentToCuratedVariantConverter curatedVariantConverter = new DocumentToCuratedVariantConverter(
                new DocumentToVariantConverter(null, null)
        );
        Document curatedVariantDocument = curatedVariantConverter.convertToStorageType(curatedVariant);
        QueryResult result = this.curatedVariantsCollection.insert(curatedVariantDocument, options);

        return result;
    }


    @Override
    public QueryResult insert(List<CuratedVariant> curatedVariants, QueryOptions options) {
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

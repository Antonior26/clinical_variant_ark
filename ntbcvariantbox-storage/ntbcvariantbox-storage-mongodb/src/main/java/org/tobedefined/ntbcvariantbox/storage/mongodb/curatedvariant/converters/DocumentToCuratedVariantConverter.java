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

package org.tobedefined.ntbcvariantbox.storage.mongodb.curatedvariant.converters;

import org.bson.Document;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.CuratedVariant;
import org.opencb.commons.datastore.core.ComplexTypeConverter;
import org.opencb.opencga.storage.mongodb.variant.converters.DocumentToVariantConverter;

import java.util.*;

/**
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class DocumentToCuratedVariantConverter implements ComplexTypeConverter<CuratedVariant, Document> {

    public static final String VARIANT = "variant";
    public static final String CLASSIFICATION = "classification";
    public static final String SCORE = "score";
    public static final String HISTORY = "history";
    public static final String EVIDENCES = "evidences";
    public static final String COMMENTS = "comments";

    private final DocumentToVariantConverter variantConverter;

    /**
     * Create a converter between {@link CuratedVariant} and {@link Document} entities
     */
    public DocumentToCuratedVariantConverter() {
        this(null);
    }


    /**
     * Create a converter between {@link CuratedVariant} and {@link Document} entities.
     *
     * @param variantConverter The object used to convert the files
     */
    public DocumentToCuratedVariantConverter(DocumentToVariantConverter variantConverter) {
        this.variantConverter = variantConverter;
    }


    @Override
    public CuratedVariant convertToDataModelType(Document object) {
        //TODO: should we inherit the variant id in the CuratedVariant????

        Document variantDocument = (Document) object.get(VARIANT);
        String classification = (String) object.get(CLASSIFICATION);
        Integer score = (Integer) object.get(SCORE);
        List history = object.get(HISTORY, List.class);
        List evidences = object.get(EVIDENCES, List.class);
        List comments = object.get(COMMENTS, List.class);

        Variant variant = variantConverter.convertToDataModelType(variantDocument);
        //TODO: create converters and convert history, evidences and comments
        CuratedVariant curatedVariant = new CuratedVariant(
                variant, classification, score, null, null, null);

        return curatedVariant;
    }

    @Override
    public Document convertToStorageType(CuratedVariant curatedVariant) {

        Variant variant = curatedVariant.getVariant();
        Document mongoVariant = variantConverter.convertToStorageType(variant);

        // The curated variant inherits the _id from the variant
        Document mongoCuratedVariant = new Document("_id", this.variantConverter.buildStorageId(variant))
                .append(CLASSIFICATION, curatedVariant.getCurationClassification())
                .append(SCORE, curatedVariant.getCurationScore())
                //TODO: convert history, evidences and comments
                //.append(HISTORY, curatedVariant.getCurationHistory())
                //.append(EVIDENCES, curatedVariant.getEvidences())
                //.append(COMMENTS, curatedVariant.getComments())
                .append(VARIANT, mongoVariant);

        return mongoCuratedVariant;
    }

}

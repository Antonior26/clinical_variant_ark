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

package org.gel.cva.storage.mongodb.knownvariant.converters;

import org.bson.Document;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.Comment;
import org.gel.models.cva.avro.CurationEntry;
import org.gel.models.cva.avro.EvidenceEntry;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.datastore.core.ComplexTypeConverter;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.variant.converters.DocumentToVariantConverter;

import java.util.*;

/**
 * @author Pablo Riesgo Ferreiro <pablo.ferreiro@genomicsengland.co.uk>
 */
public class DocumentToKnownVariantConverter implements ComplexTypeConverter<KnownVariantWrapper, Document> {

    public static final String SUBMITTER = "submitter";
    public static final String VARIANT = "variant";
    public static final String CURATIONS = "curations";
    public static final String EVIDENCES = "evidences";
    public static final String COMMENTS = "comments";

    private final DocumentToVariantConverter variantConverter;
    private final DocumentToEvidenceEntryConverter evidenceEntryConverter;
    private final DocumentToCurationEntryConverter curationEntryConverter;
    private final DocumentToCommentConverter commentConverter;

    /**
     * Create a converter between {@link KnownVariantWrapper} and {@link Document} entities.
     *
     */
    public DocumentToKnownVariantConverter() {
        this.variantConverter = new DocumentToVariantConverter(null, null);
        this.evidenceEntryConverter = new DocumentToEvidenceEntryConverter();
        this.curationEntryConverter = new DocumentToCurationEntryConverter();
        this.commentConverter = new DocumentToCommentConverter();
    }

    @Override
    public KnownVariantWrapper convertToDataModelType(Document object) {
        //TODO: should we inherit the variant id in the CuratedVariant????
        String submitter = (String) object.get(SUBMITTER);
        Document variantDocument = (Document) object.get(VARIANT);
        List<Document> curationsDocs = object.get(CURATIONS, List.class);
        List<Document> evidencesDocs = object.get(EVIDENCES, List.class);
        List<Document> commentsDocs = object.get(COMMENTS, List.class);
        // Converts Variant
        Variant variant = variantConverter.convertToDataModelType(variantDocument);
        // Creates the known variant
        KnownVariantWrapper curatedVariant = null;
        try {
            curatedVariant = new KnownVariantWrapper(
                    submitter,
                    variant);
        }
        catch (VariantAnnotatorException e) {
            //TODO: add this exception to the signature and raise appropriately
        }
        catch (CvaException e) {
            //TODO: add this exception to the signature and raise appropriately
        }
        // Converts list of evidences
        List<EvidenceEntry> evidences = new LinkedList<>();
        if (evidencesDocs != null) {
            for (Document evidencesDoc : evidencesDocs) {
                evidences.add(this.evidenceEntryConverter.convertToDataModelType(evidencesDoc));
            }
        }
        curatedVariant.getImpl().setEvidences(evidences);
        // Converts curation history
        List<CurationEntry> curations = new LinkedList<>();
        if (curationsDocs != null) {
            for (Document curationDoc : curationsDocs) {
                curations.add(this.curationEntryConverter.convertToDataModelType(curationDoc));
            }
        }
        curatedVariant.getImpl().setCurations(curations);
        // Converts comments
        List<Comment> comments = new LinkedList<>();
        if (commentsDocs != null) {
            for (Document commentsDoc : commentsDocs) {
                comments.add(this.commentConverter.convertToDataModelType(commentsDoc));
            }
        }
        curatedVariant.getImpl().setComments(comments);
        return curatedVariant;
    }

    @Override
    public Document convertToStorageType(KnownVariantWrapper curatedVariant) {

        Variant variant = curatedVariant.getVariant();
        Document mongoVariant;
        if (variant != null) {
            mongoVariant = variantConverter.convertToStorageType(variant);
        }
        else {
            // sets an empty document in case there is no variant
            mongoVariant = new Document();
        }

        // The curated variant inherits the _id from the variant
        Document mongoCuratedVariant = new Document("_id", this.variantConverter.buildStorageId(variant))
                .append(SUBMITTER, curatedVariant.getImpl().getSubmitter())
                .append(VARIANT, mongoVariant);
        // Converts list of evidences
        List<Document> evidences = new LinkedList<>();
        if (curatedVariant.getImpl().getEvidences() != null) {
            for (EvidenceEntry evidenceEntry:  curatedVariant.getImpl().getEvidences()) {
                evidences.add(this.evidenceEntryConverter.convertToStorageType(evidenceEntry));
            }
        }
        mongoCuratedVariant.append(EVIDENCES, evidences);
        // Converts curations
        List<Document> curations = new LinkedList<>();
        if (curatedVariant.getImpl().getCurations() != null) {
            for (CurationEntry curationEntry: curatedVariant.getImpl().getCurations()) {
                curations.add(this.curationEntryConverter.convertToStorageType(curationEntry));
            }
        }
        mongoCuratedVariant.append(CURATIONS, curations);
        // Converts comments
        List<Document> comments = new LinkedList<>();
        if (curatedVariant.getImpl().getComments() != null) {

            for (Comment comment: curatedVariant.getImpl().getComments()) {
                comments.add(this.commentConverter.convertToStorageType(comment));
            }
        }
        mongoCuratedVariant.append(COMMENTS, comments);
        return mongoCuratedVariant;
    }

}

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
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.Comment;
import org.gel.models.cva.avro.CurationHistoryEntry;
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

    public static final String VARIANT = "variant";
    public static final String CLASSIFICATION = "classification";
    public static final String SCORE = "curationScore";
    public static final String HISTORY = "history";
    public static final String EVIDENCES = "evidences";
    public static final String COMMENTS = "comments";

    private final DocumentToVariantConverter variantConverter;
    private final DocumentToEvidenceEntryConverter evidenceEntryConverter;
    private final DocumentToCommentConverter commentConverter;

    /**
     * Create a converter between {@link KnownVariantWrapper} and {@link Document} entities.
     *
     */
    public DocumentToKnownVariantConverter() {
        this.variantConverter = new DocumentToVariantConverter(null, null);
        this.evidenceEntryConverter = new DocumentToEvidenceEntryConverter();
        this.commentConverter = new DocumentToCommentConverter();
    }

    @Override
    public KnownVariantWrapper convertToDataModelType(Document object) {
        //TODO: should we inherit the variant id in the CuratedVariant????
        Document variantDocument = (Document) object.get(VARIANT);
        String classification = (String) object.get(CLASSIFICATION);
        Integer score = Integer.parseInt((String)object.get(SCORE));
        List<Document> historyDocs = object.get(HISTORY, List.class);
        List<Document> evidencesDocs = object.get(EVIDENCES, List.class);
        List<Document> commentsDocs = object.get(COMMENTS, List.class);
        // Converts Variant
        Variant variant = variantConverter.convertToDataModelType(variantDocument);
        // Converts list of evidences
        List<EvidenceEntry> evidences = new LinkedList<EvidenceEntry>();
        if (evidencesDocs != null) {
            for (Document evidencesDoc : evidencesDocs) {
                evidences.add(this.evidenceEntryConverter.convertToDataModelType(evidencesDoc));
            }
        }
        // Converts curation history
        List<CurationHistoryEntry> curationHistory = new LinkedList<CurationHistoryEntry>();
        if (historyDocs != null) {
            for (Document historyDoc : historyDocs) {
                //evidences.add(this.evidenceEntryConverter.convertToDataModelType(evidenceEntry));
            }
        }
        // Converts comments
        List<Comment> comments = new LinkedList<Comment>();
        if (commentsDocs != null) {
            for (Document commentsDoc : commentsDocs) {
                comments.add(this.commentConverter.convertToDataModelType(commentsDoc));
            }
        }
        //TODO: create converters and convert history
        KnownVariantWrapper curatedVariant = null;
        try {
            curatedVariant = new KnownVariantWrapper(
                    variant, classification, score, curationHistory, evidences, comments);
        }
        catch (VariantAnnotatorException e) {
            //TODO: manage error
        }
        catch (IllegalCvaConfigurationException e) {
            //TODO: manage error
        }
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
                .append(CLASSIFICATION, curatedVariant.getCurationClassification())
                .append(SCORE, curatedVariant.getCurationScore())
                .append(VARIANT, mongoVariant);
        // Converts list of evidences
        List<Document> evidences = new LinkedList<Document>();
        if (curatedVariant.getEvidences() != null) {
            for (EvidenceEntry evidenceEntry: (List<EvidenceEntry>) curatedVariant.getEvidences()) {
                evidences.add(this.evidenceEntryConverter.convertToStorageType(evidenceEntry));
            }
        }
        mongoCuratedVariant.append(EVIDENCES, evidences);
        // Converts history
        List<Document> curationHistory = new LinkedList<Document>();
        if (curatedVariant.getCurationHistory() != null) {

            for (CurationHistoryEntry curationHistoryEntry: (List<CurationHistoryEntry>) curatedVariant.getCurationHistory()) {
                //curationHistory.add(this.curationHistoryEntryConverter.convertToStorageType(curationHistoryEntry));
                //TODO: use converter
            }
        }
        mongoCuratedVariant.append(HISTORY, curationHistory);
        // Converts comments
        List<Document> comments = new LinkedList<Document>();
        if (curatedVariant.getComments() != null) {

            for (Comment comment: (List<Comment>) curatedVariant.getComments()) {
                comments.add(this.commentConverter.convertToStorageType(comment));
            }
        }
        mongoCuratedVariant.append(COMMENTS, comments);
        return mongoCuratedVariant;
    }

}

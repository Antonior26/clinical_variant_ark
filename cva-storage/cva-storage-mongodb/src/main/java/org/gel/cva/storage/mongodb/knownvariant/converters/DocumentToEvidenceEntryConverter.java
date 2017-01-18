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
import org.gel.cva.storage.core.knownvariant.dto.KnownVariant;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.*;
import org.opencb.commons.datastore.core.ComplexTypeConverter;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Pablo Riesgo Ferreiro <pablo.ferreiro@genomicsengland.co.uk>
 */
public class DocumentToEvidenceEntryConverter implements ComplexTypeConverter<EvidenceEntry, Document> {

    public static final String DATE = "date";
    public static final String SUBMITTER = "submitter";
    public static final String SOURCE_NAME = "sourceName";
    public static final String SOURCE_CLASS = "sourceClass";
    public static final String SOURCE_VERSION = "sourceVersion";
    public static final String SOURCE_URL = "sourceUrl";
    public static final String ALLELE_ORIGIN = "alleleOrigin";
    public static final String PHENOTYPES = "phenotypes";
    public static final String PHENOTYPE = "phenotype";
    public static final String INHERITANCE_MODE = "inheritanceMode";
    public static final String PUBMED_ID = "pubmedId";
    public static final String STUDY = "study";
    public static final String NUMBER_INDIVIDUALS = "numberIndividuals";
    public static final String ETHNICITY = "ethnicity";
    public static final String DESCRIPTION = "description";
    public static final String COMMENTS = "comments";

    private static DocumentToCommentConverter commentConverter;

    /**
     * Create a converter between {@link KnownVariant} and {@link Document} entities
     */
    public DocumentToEvidenceEntryConverter(DocumentToCommentConverter commentConverter) {
        this.commentConverter = commentConverter;
    }


    @Override
    public EvidenceEntry convertToDataModelType(Document object) {
        EvidenceEntry evidenceEntry = new EvidenceEntry();
        evidenceEntry.setDate((Long) object.get(DATE));
        evidenceEntry.setSubmitter((String) object.get(SUBMITTER));
        evidenceEntry.setSourceName((String) object.get(SOURCE_NAME));
        evidenceEntry.setSourceClass(SourceClass.valueOf((String) object.get(SOURCE_CLASS)));
        evidenceEntry.setSourceVersion((String) object.get(SOURCE_VERSION));
        evidenceEntry.setSourceUrl((String) object.get(SOURCE_URL));
        evidenceEntry.setAlleleOrigin(AlleleOrigin.valueOf((String) object.get(ALLELE_ORIGIN)));
        // Parses phenotypes
        List<Document> phenotypesDocs = (List<Document>) object.get(PHENOTYPES);
        List<EvidencePhenotype> phenotypes = new LinkedList<EvidencePhenotype>();
        for (Document phenotypeDoc: phenotypesDocs) {
            EvidencePhenotype evidencePhenotype = new EvidencePhenotype();
            evidencePhenotype.setPhenotype((String)phenotypeDoc.get(PHENOTYPE));
            evidencePhenotype.setInheritanceMode(ReportedModeOfInheritance.valueOf((String)phenotypeDoc.get(INHERITANCE_MODE)));
            phenotypes.add(evidencePhenotype);
        }
        evidenceEntry.setPhenotypes(phenotypes);
        evidenceEntry.setPubmedId((String) object.get(PUBMED_ID));
        evidenceEntry.setStudy((String) object.get(STUDY));
        evidenceEntry.setNumberIndividuals((Integer) object.get(NUMBER_INDIVIDUALS));
        evidenceEntry.setEthnicity(EthnicCategory.valueOf((String) object.get(ETHNICITY)));
        evidenceEntry.setDescription((String) object.get(DESCRIPTION));
        // Parses comments
        List<Document> commentsDocs = (List<Document>) object.get(COMMENTS);
        List<Comment> comments = new LinkedList<Comment>();
        for (Document commentDoc: commentsDocs) {
            Comment comment = commentConverter.convertToDataModelType(commentDoc);
            comments.add(comment);
        }
        evidenceEntry.setComments(comments);
        return evidenceEntry;
    }

    @Override
    public Document convertToStorageType(EvidenceEntry evidenceEntry) {
        // Creates an EvidenceEntry with the compulsory fields
        Document mongoEvidenceEntry = new Document();
        mongoEvidenceEntry.append(DATE, evidenceEntry.getDate());
        mongoEvidenceEntry.append(SUBMITTER, evidenceEntry.getSubmitter());
        if (evidenceEntry.getSourceName() != null) {
            mongoEvidenceEntry.append(SOURCE_NAME, evidenceEntry.getSourceName());
        }
        mongoEvidenceEntry.append(SOURCE_CLASS, evidenceEntry.getSourceClass().toString());
        if (evidenceEntry.getSourceVersion() != null) {
            mongoEvidenceEntry.append(SOURCE_VERSION, evidenceEntry.getSourceVersion());
        }
        if (evidenceEntry.getSourceUrl() != null) {
            mongoEvidenceEntry.append(SOURCE_URL, evidenceEntry.getSourceUrl());
        }
        mongoEvidenceEntry.append(ALLELE_ORIGIN, evidenceEntry.getAlleleOrigin().toString());
        // Parses phenotypes
        if (evidenceEntry.getPhenotypes() != null) {
            List<EvidencePhenotype> phenotypes = evidenceEntry.getPhenotypes();
            List<Document> phenotypesDocs = new LinkedList<Document>();
            for (EvidencePhenotype evidencePhenotype: phenotypes) {
                phenotypesDocs.add(new Document()
                        .append(PHENOTYPE, evidencePhenotype.getPhenotype())
                        .append(INHERITANCE_MODE, evidencePhenotype.getInheritanceMode().toString())
                );
            }
            mongoEvidenceEntry.append(PHENOTYPES, phenotypesDocs);
        }
        if (evidenceEntry.getPubmedId() != null) {
            mongoEvidenceEntry.append(PUBMED_ID, evidenceEntry.getPubmedId());
        }
        if (evidenceEntry.getStudy() != null) {
            mongoEvidenceEntry.append(STUDY, evidenceEntry.getStudy());
        }
        if (evidenceEntry.getEthnicity() != null) {
            mongoEvidenceEntry.append(ETHNICITY, evidenceEntry.getEthnicity().toString());
        }
        if (evidenceEntry.getNumberIndividuals() != null) {
            mongoEvidenceEntry.append(NUMBER_INDIVIDUALS, evidenceEntry.getNumberIndividuals());
        }
        if (evidenceEntry.getDescription() != null) {
            mongoEvidenceEntry.append(DESCRIPTION, evidenceEntry.getDescription());
        }
        // Parses comments
        if (evidenceEntry.getComments() != null) {
            List<Comment> comments = evidenceEntry.getComments();
            List<Document> commentsDocs = new LinkedList<Document>();
            for (Comment comment: comments) {
                commentsDocs.add(commentConverter.convertToStorageType(comment));
            }
            mongoEvidenceEntry.append(COMMENTS, commentsDocs);
        }
        return mongoEvidenceEntry;
    }

}

package org.gel.cva.storage.mongodb.knownvariant.converters;

import org.bson.Document;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by priesgo on 17/01/17.
 */
public class DocumentToEvidenceEntryConverterTest {

    DocumentToEvidenceEntryConverter evidenceEntryConverter;
    Date now = new Date();
    Long date = now.getTime();
    String submitter = "Mr.Test";
    String sourceName = "RiskDB";
    SourceClass sourceClass = SourceClass.unknown;
    String sourceVersion = "latest";
    String sourceUrl = "http://yourref.com";
    AlleleOrigin alleleOrigin = AlleleOrigin.unknown;
    List<EvidencePhenotype> phenotypes = new LinkedList<EvidencePhenotype>();
    String pubmedId = "12345";
    String study = "any_study";
    Integer numberIndividuals = 4;
    EthnicCategory ethnicity = EthnicCategory.A;
    String description = "this reference is an important evidence";
    List<Comment> comments = new LinkedList<Comment>();

    @Before
    public void setup() {
        this.evidenceEntryConverter = new DocumentToEvidenceEntryConverter(new DocumentToCommentConverter());
        EvidencePhenotype evidencePhenotype = new EvidencePhenotype("SO:000001", ReportedModeOfInheritance.biallelic);
        EvidencePhenotype evidencePhenotype2 = new EvidencePhenotype("SO:000002", ReportedModeOfInheritance.mitochondrial);
        this.phenotypes.add(evidencePhenotype);
        this.phenotypes.add(evidencePhenotype2);
        Comment comment = new Comment();
        comment.setDate(new Long(1234));
        comment.setAuthor("author_comment1");
        comment.setText("a very interesting comment");
        Comment comment2 = new Comment();
        comment2.setDate(new Long(5678));
        comment2.setAuthor("author_comment2");
        comment2.setText("a more interesting comment");
        this.comments.add(comment);
        this.comments.add(comment2);
    }

    @Test
    public void convertToDataModelTypeTest() {
        Document document = new Document();
        document.append(DocumentToEvidenceEntryConverter.DATE, this.date);
        document.append(DocumentToEvidenceEntryConverter.SUBMITTER, this.submitter);
        document.append(DocumentToEvidenceEntryConverter.SOURCE_NAME, this.sourceName);
        document.append(DocumentToEvidenceEntryConverter.SOURCE_CLASS, this.sourceClass.toString());
        document.append(DocumentToEvidenceEntryConverter.SOURCE_VERSION, this.sourceVersion);
        document.append(DocumentToEvidenceEntryConverter.SOURCE_URL, this.sourceUrl);
        document.append(DocumentToEvidenceEntryConverter.ALLELE_ORIGIN, this.alleleOrigin.toString());
        List<Document> phenotypesDocuments = new LinkedList<Document>();
        for (EvidencePhenotype phenotype : this.phenotypes) {
            Document phenotypeDocument = new Document();
            phenotypeDocument.append(DocumentToEvidenceEntryConverter.PHENOTYPE, phenotype.getPhenotype());
            phenotypeDocument.append(DocumentToEvidenceEntryConverter.INHERITANCE_MODE,
                    phenotype.getInheritanceMode().toString());
            phenotypesDocuments.add(phenotypeDocument);
        }
        document.append(DocumentToEvidenceEntryConverter.PHENOTYPES, phenotypesDocuments);
        document.append(DocumentToEvidenceEntryConverter.PUBMED_ID, this.pubmedId);
        document.append(DocumentToEvidenceEntryConverter.STUDY, this.study);
        document.append(DocumentToEvidenceEntryConverter.NUMBER_INDIVIDUALS, this.numberIndividuals);
        document.append(DocumentToEvidenceEntryConverter.ETHNICITY, this.ethnicity.toString());
        document.append(DocumentToEvidenceEntryConverter.DESCRIPTION, this.description);
        List<Document> commentsDocuments = new LinkedList<Document>();
        for (Comment comment: this.comments) {
            Document commentDocument = new Document();
            commentDocument.append("text", comment.getText());
            commentDocument.append("author", comment.getAuthor());
            commentDocument.append("date", comment.getDate());
            commentsDocuments.add(commentDocument);
        }
        document.append(DocumentToEvidenceEntryConverter.COMMENTS, commentsDocuments);
        EvidenceEntry evidenceEntry = evidenceEntryConverter.convertToDataModelType(document);
        assertEquals(evidenceEntry.getDate(), this.date);
        assertEquals(evidenceEntry.getSubmitter(), this.submitter);
        assertEquals(evidenceEntry.getSourceName(), this.sourceName);
        assertEquals(evidenceEntry.getSourceClass(), this.sourceClass);
        assertEquals(evidenceEntry.getSourceVersion(), this.sourceVersion);
        assertEquals(evidenceEntry.getSourceUrl(), this.sourceUrl);
        assertEquals(evidenceEntry.getAlleleOrigin(), this.alleleOrigin);
        assertEquals(evidenceEntry.getPhenotypes().size(), this.phenotypes.size());
        assertEquals(evidenceEntry.getPubmedId(), this.pubmedId);
        assertEquals(evidenceEntry.getStudy(), this.study);
        assertEquals(evidenceEntry.getNumberIndividuals(), this.numberIndividuals);
        assertEquals(evidenceEntry.getEthnicity(), this.ethnicity);
        assertEquals(evidenceEntry.getDescription(), this.description);
        assertEquals(evidenceEntry.getComments().size(), this.comments.size());
    }

    @Test
    public void convertToStorageTypeTest() {
        EvidenceEntry evidenceEntry = new EvidenceEntry();
        evidenceEntry.setDate(this.date);
        evidenceEntry.setSubmitter(this.submitter);
        evidenceEntry.setSourceName(this.sourceName);
        evidenceEntry.setSourceClass(this.sourceClass);
        evidenceEntry.setSourceVersion(this.sourceVersion);
        evidenceEntry.setSourceUrl(this.sourceUrl);
        evidenceEntry.setAlleleOrigin(this.alleleOrigin);
        evidenceEntry.setPhenotypes(this.phenotypes);
        evidenceEntry.setPubmedId(this.pubmedId);
        evidenceEntry.setStudy(this.study);
        evidenceEntry.setNumberIndividuals(this.numberIndividuals);
        evidenceEntry.setEthnicity(this.ethnicity);
        evidenceEntry.setDescription(this.description);
        evidenceEntry.setComments(this.comments);
        Document document = evidenceEntryConverter.convertToStorageType(evidenceEntry);
        assertEquals(document.get(DocumentToEvidenceEntryConverter.DATE), this.date);
        assertEquals(document.get(DocumentToEvidenceEntryConverter.SUBMITTER), this.submitter);
        assertEquals(document.get(DocumentToEvidenceEntryConverter.SOURCE_NAME), this.sourceName);
        assertEquals(document.get(DocumentToEvidenceEntryConverter.SOURCE_CLASS), this.sourceClass.toString());
        assertEquals(document.get(DocumentToEvidenceEntryConverter.SOURCE_VERSION), this.sourceVersion);
        assertEquals(document.get(DocumentToEvidenceEntryConverter.SOURCE_URL), this.sourceUrl);
        assertEquals(document.get(DocumentToEvidenceEntryConverter.ALLELE_ORIGIN), this.alleleOrigin.toString());
        assertEquals(((List)document.get(DocumentToEvidenceEntryConverter.PHENOTYPES)).size(), this.phenotypes.size());
        assertEquals(document.get(DocumentToEvidenceEntryConverter.PUBMED_ID), this.pubmedId);
        assertEquals(document.get(DocumentToEvidenceEntryConverter.STUDY), this.study);
        assertEquals(document.get(DocumentToEvidenceEntryConverter.NUMBER_INDIVIDUALS), this.numberIndividuals);
        assertEquals(document.get(DocumentToEvidenceEntryConverter.ETHNICITY), this.ethnicity.toString());
        assertEquals(document.get(DocumentToEvidenceEntryConverter.DESCRIPTION), this.description);
        assertEquals(((List)document.get(DocumentToEvidenceEntryConverter.COMMENTS)).size(), this.comments.size());
    }
}
